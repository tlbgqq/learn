package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.*;
import com.studyagent.entity.*;
import com.studyagent.mapper.QuestionMapper;
import com.studyagent.mapper.SprintRecordMapper;
import com.studyagent.mapper.SprintSessionMapper;
import com.studyagent.mapper.StudentAnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintService {

    private static final int SPRINT_DURATION = 60;
    private static final int BASE_SCORE = 100;
    private static final int BASE_GOLD = 10;
    private static final int BASE_EXP = 5;
    private final SprintSessionMapper sessionMapper;
    private final SprintRecordMapper recordMapper;
    private final QuestionMapper questionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final StudentService studentService;
    private final KnowledgeMasteryService knowledgeMasteryService;
    private final ObjectMapper objectMapper;

    @Transactional
    public SprintStartResponse startSprint(Long studentId, Long subjectId) {
        Student student = studentService.findById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        String sessionId = generateSessionId();

        SprintSession session = new SprintSession();
        session.setSessionId(sessionId);
        session.setStudentId(studentId);
        session.setSubjectId(subjectId != null ? subjectId : getDefaultSubjectId(student.getGradeId()));
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ACTIVE");
        session.setUsedQuestionIds("[]");
        sessionMapper.insert(session);

        Question question = getRandomQuestion(session);

        return SprintStartResponse.builder()
                .sessionId(sessionId)
                .startTime(session.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .duration(SPRINT_DURATION)
                .subjectId(session.getSubjectId())
                .question(toQuestionResponse(question))
                .build();
    }

    public SprintQuestionResponse getNextQuestion(String sessionId) {
        SprintSession session = getSession(sessionId);
        if (!"ACTIVE".equals(session.getStatus())) {
            return null;
        }
        Question question = getRandomQuestion(session);
        return question != null ? toQuestionResponse(question) : null;
    }

    @Transactional
    public SprintAnswerResponse submitAnswer(String sessionId, Long studentId, Long questionId,
                                            String answer, Double answerTime) {
        SprintSession session = getSession(sessionId);
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new RuntimeException("会话已结束");
        }

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        boolean isCorrect = QuestionService.isAnswerCorrect(question.getAnswer(), answer);

        List<Long> usedIds = parseUsedQuestionIds(session.getUsedQuestionIds());
        if (!usedIds.contains(questionId)) {
            usedIds.add(questionId);
            session.setUsedQuestionIds(toJson(usedIds));
            sessionMapper.updateById(session);
        }

        int comboCount = 0;
        int score = 0;
        int gold = 0;
        int exp = 0;
        int diamond = 0;

        if (isCorrect) {
            comboCount = calculateCombo(sessionId, studentId, true);
            int maxCombo = getMaxCombo(sessionId);
            if (comboCount > maxCombo) {
                updateMaxCombo(sessionId, comboCount);
            }

            double timeBonus = calculateTimeBonus(answerTime);
            double comboBonus = calculateComboBonus(comboCount);
            score = (int) (BASE_SCORE * timeBonus * comboBonus);

            gold = (int) (BASE_GOLD * (1 + comboCount * 0.2) * (1 + question.getDifficulty() * 0.1));
            exp = (int) (BASE_EXP * (1 + comboCount * 0.2) * (1 + question.getDifficulty() * 0.1));
            diamond = calculateDiamondReward(comboCount);

            studentService.addExpAndGold(studentId, exp, gold);
            if (diamond > 0) {
                studentService.addDiamond(studentId, diamond);
            }

            incrementCorrect(sessionId, studentId);

            // 更新知识点掌握度
            if (question.getKnowledgePointIds() != null && !question.getKnowledgePointIds().isEmpty()) {
                for (String kpIdStr : question.getKnowledgePointIds().split(",")) {
                    try {
                        Long kpId = Long.parseLong(kpIdStr.trim());
                        knowledgeMasteryService.updateMastery(studentId, kpId, true);
                    } catch (NumberFormatException e) {
                        log.warn("解析知识点ID失败: {}", kpIdStr);
                    }
                }
            }

            markWrongAnswersAsCorrected(studentId, questionId);
        } else {
            resetCombo(sessionId, studentId);
            incrementWrong(sessionId);

            // 记录错题
            StudentAnswer wrongAnswer = new StudentAnswer();
            wrongAnswer.setStudentId(studentId);
            wrongAnswer.setQuestionId(questionId);
            wrongAnswer.setAnswer(answer);
            wrongAnswer.setIsCorrect(false);
            if (question.getKnowledgePointIds() != null && !question.getKnowledgePointIds().isEmpty()) {
                try {
                    wrongAnswer.setKnowledgePointId(Long.parseLong(question.getKnowledgePointIds().split(",")[0]));
                } catch (NumberFormatException e) {
                    log.warn("解析知识点ID失败: {}", question.getKnowledgePointIds());
                }
            }
            studentAnswerMapper.insert(wrongAnswer);
        }

        Question nextQuestion = getRandomQuestion(session);

        return SprintAnswerResponse.builder()
                .isCorrect(isCorrect)
                .correctAnswer(question.getAnswer())
                .comboCount(comboCount)
                .timeBonus(calculateTimeBonus(answerTime))
                .comboBonus(calculateComboBonus(comboCount))
                .score(score)
                .reward(SprintAnswerResponse.RewardDto.builder()
                        .gold(gold)
                        .exp(exp)
                        .diamond(diamond)
                        .build())
                .nextQuestion(nextQuestion != null ? toQuestionResponse(nextQuestion) : null)
                .build();
    }

    @Transactional
    public SprintResultResponse finishSprint(String sessionId, Long studentId, String reason) {
        SprintSession session = getSession(sessionId);
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new RuntimeException("会话已结束");
        }

        session.setStatus("FINISHED");
        session.setEndTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        SprintRecord record = buildSprintRecord(session, studentId, reason);

        Integer previousBest = getBestScore(studentId);
        boolean isNewRecord = previousBest == null || record.getTotalScore() > previousBest;
        record.setIsNewRecord(isNewRecord);

        recordMapper.insert(record);

        int totalGold = 0;
        int totalExp = 0;
        int totalDiamond = 0;

        List<SprintAnswerResponse.RewardDto> rewards = getSessionRewards(sessionId);
        for (SprintAnswerResponse.RewardDto r : rewards) {
            totalGold += r.getGold();
            totalExp += r.getExp();
            totalDiamond += r.getDiamond();
        }

        return SprintResultResponse.builder()
                .sessionId(sessionId)
                .totalScore(record.getTotalScore())
                .totalQuestions(record.getTotalQuestions())
                .correctCount(record.getCorrectCount())
                .wrongCount(record.getWrongCount())
                .accuracy(record.getAccuracy().doubleValue())
                .maxCombo(record.getMaxCombo())
                .duration(record.getDuration())
                .rewards(SprintResultResponse.RewardDto.builder()
                        .gold(totalGold)
                        .exp(totalExp)
                        .diamond(totalDiamond)
                        .build())
                .isNewRecord(isNewRecord)
                .previousBest(previousBest != null ? previousBest : 0)
                .build();
    }

    public List<SprintHistoryResponse> getHistory(Long studentId, Integer limit) {
        LambdaQueryWrapper<SprintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SprintRecord::getStudentId, studentId)
                .orderByDesc(SprintRecord::getCreateTime)
                .last("LIMIT " + (limit != null ? limit : 10));

        List<SprintRecord> records = recordMapper.selectList(wrapper);

        List<SprintHistoryResponse> result = new ArrayList<>();
        for (SprintRecord r : records) {
            result.add(SprintHistoryResponse.builder()
                    .sessionId(r.getSessionId())
                    .date(r.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .subjectName(getSubjectName(r.getSubjectId()))
                    .totalScore(r.getTotalScore())
                    .totalQuestions(r.getTotalQuestions())
                    .correctCount(r.getCorrectCount())
                    .accuracy(r.getAccuracy())
                    .maxCombo(r.getMaxCombo())
                    .duration(r.getDuration())
                    .build());
        }
        return result;
    }

    public SprintBestResponse getBestRecord(Long studentId) {
        LambdaQueryWrapper<SprintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SprintRecord::getStudentId, studentId);
        List<SprintRecord> records = recordMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return SprintBestResponse.builder()
                    .bestScore(0)
                    .bestCombo(0)
                    .bestAccuracy(0.0)
                    .totalSessions(0)
                    .totalQuestions(0)
                    .totalCorrect(0)
                    .build();
        }

        int bestScore = 0;
        int bestCombo = 0;
        double bestAccuracy = 0.0;
        int totalQuestions = 0;
        int totalCorrect = 0;

        for (SprintRecord r : records) {
            if (r.getTotalScore() > bestScore) bestScore = r.getTotalScore();
            if (r.getMaxCombo() > bestCombo) bestCombo = r.getMaxCombo();
            if (r.getAccuracy().doubleValue() > bestAccuracy) bestAccuracy = r.getAccuracy().doubleValue();
            totalQuestions += r.getTotalQuestions();
            totalCorrect += r.getCorrectCount();
        }

        return SprintBestResponse.builder()
                .bestScore(bestScore)
                .bestCombo(bestCombo)
                .bestAccuracy(bestAccuracy)
                .totalSessions(records.size())
                .totalQuestions(totalQuestions)
                .totalCorrect(totalCorrect)
                .build();
    }

    private SprintSession getSession(String sessionId) {
        SprintSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<SprintSession>()
                        .eq(SprintSession::getSessionId, sessionId)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        return session;
    }

    private String generateSessionId() {
        return "SPRINT_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    private Long getDefaultSubjectId(Integer gradeId) {
        return 3L;
    }

    private Question getRandomQuestion(SprintSession session) {
        List<Long> usedIds = parseUsedQuestionIds(session.getUsedQuestionIds());

        // 获取学生对该科目各知识点的掌握度
        Map<Long, Double> masteryMap = knowledgeMasteryService
                .getStudentMasteryMap(session.getStudentId(), session.getSubjectId());

        // 获取该科目所有可用题目
        List<Question> allQuestions = questionMapper.selectBySubjectId(session.getSubjectId());
        List<Question> questions = allQuestions.stream()
                .filter(q -> !usedIds.contains(q.getId()))
                .collect(Collectors.toList());

        if (questions.isEmpty()) {
            return null;
        }

        // 根据知识点掌握度加权选择：掌握度越低权重越高
        List<Double> weights = questions.stream()
                .map(q -> calculateQuestionWeight(q, masteryMap))
                .collect(Collectors.toList());

        // 加权随机选择
        return weightedRandomSelect(questions, weights);
    }

    private double calculateQuestionWeight(Question q, Map<Long, Double> masteryMap) {
        if (q.getKnowledgePointIds() == null || q.getKnowledgePointIds().isEmpty()) {
            return 0.5; // 无知识点标记的题目使用中间权重
        }

        double minMastery = 1.0;
        for (String kpIdStr : q.getKnowledgePointIds().split(",")) {
            try {
                Long kpId = Long.parseLong(kpIdStr.trim());
                Double mastery = masteryMap.getOrDefault(kpId, 0.0);
                if (mastery < minMastery) {
                    minMastery = mastery;
                }
            } catch (NumberFormatException e) {
                // 忽略无效ID
            }
        }
        // 掌握度越低，权重越高（确保完全掌握的题目很少被选）
        return Math.max(0.05, 1.0 - minMastery);
    }

    private Question weightedRandomSelect(List<Question> questions, List<Double> weights) {
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < questions.size(); i++) {
            cumulative += weights.get(i);
            if (random <= cumulative) {
                return questions.get(i);
            }
        }
        return questions.get(questions.size() - 1);
    }

    private SprintQuestionResponse toQuestionResponse(Question question) {
        if (question == null) return null;

        List<String> options = new ArrayList<>();
        if ("选择".equals(question.getType()) && question.getOptions() != null && !question.getOptions().isEmpty()) {
            try {
                options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.error("解析选项失败，使用默认值", e);
                options = Arrays.asList("A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4");
            }
        }

        return SprintQuestionResponse.builder()
                .id(question.getId())
                .type(question.getType())
                .content(question.getContent())
                .options(options)
                .difficulty(question.getDifficulty())
                .build();
    }

    private double calculateTimeBonus(Double answerTime) {
        if (answerTime == null) return 1.0;
        if (answerTime < 30) return 1.0;
        if (answerTime < 45) return 1.2;
        return 1.5;
    }

    private double calculateComboBonus(int combo) {
        return Math.min(1.0 + combo * 0.1, 3.0);
    }

    private int calculateDiamondReward(int combo) {
        if (combo >= 20) return 50;
        if (combo >= 10) return 20;
        if (combo >= 5) return 5;
        if (combo >= 3) return 1;
        return 0;
    }

    private List<Long> parseUsedQuestionIds(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("解析已用题目ID失败", e);
            return new ArrayList<>();
        }
    }

    private String toJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            log.error("序列化已用题目ID失败", e);
            return "[]";
        }
    }

    private int calculateCombo(String sessionId, Long studentId, boolean correct) {
        String key = "sprint_combo_" + sessionId;
        try {
            String comboStr = getSessionData(key);
            return comboStr != null ? Integer.parseInt(comboStr) + 1 : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private void resetCombo(String sessionId, Long studentId) {
    }

    private int getMaxCombo(String sessionId) {
        String key = "sprint_max_combo_" + sessionId;
        try {
            String comboStr = getSessionData(key);
            return comboStr != null ? Integer.parseInt(comboStr) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateMaxCombo(String sessionId, int combo) {
    }

    private void incrementCorrect(String sessionId, Long studentId) {
    }

    private void incrementWrong(String sessionId) {
    }

    private String getSessionData(String key) {
        return null;
    }

    private void markWrongAnswersAsCorrected(Long studentId, Long questionId) {
        List<StudentAnswer> wrongAnswers = studentAnswerMapper.selectList(
                new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getStudentId, studentId)
                        .eq(StudentAnswer::getQuestionId, questionId)
                        .eq(StudentAnswer::getIsCorrect, false)
                        .eq(StudentAnswer::getIsCorrected, false)
        );

        for (StudentAnswer wrongAnswer : wrongAnswers) {
            wrongAnswer.setIsCorrected(true);
            studentAnswerMapper.updateById(wrongAnswer);
            log.info("标记错题为已改正，studentAnswerId: {}, questionId: {}", wrongAnswer.getId(), questionId);
        }
    }

    private Integer getBestScore(Long studentId) {
        LambdaQueryWrapper<SprintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SprintRecord::getStudentId, studentId)
                .orderByDesc(SprintRecord::getTotalScore)
                .last("LIMIT 1");
        SprintRecord record = recordMapper.selectOne(wrapper);
        return record != null ? record.getTotalScore() : null;
    }

    private SprintRecord buildSprintRecord(SprintSession session, Long studentId, String reason) {
        SprintRecord record = new SprintRecord();
        record.setSessionId(session.getSessionId());
        record.setStudentId(studentId);
        record.setSubjectId(session.getSubjectId());
        record.setTotalScore(0);
        record.setTotalQuestions(0);
        record.setCorrectCount(0);
        record.setWrongCount(0);
        record.setAccuracy(BigDecimal.ZERO);
        record.setMaxCombo(0);
        record.setDuration(SPRINT_DURATION);
        record.setEndReason(reason != null ? reason : "TIMEOUT");
        return record;
    }

    private List<SprintAnswerResponse.RewardDto> getSessionRewards(String sessionId) {
        return new ArrayList<>();
    }

    private String getSubjectName(Long subjectId) {
        return "英语";
    }
}
