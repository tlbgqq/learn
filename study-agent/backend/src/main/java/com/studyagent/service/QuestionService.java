package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.dto.WrongAnswerResponse;
import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Question;
import com.studyagent.entity.StudentAnswer;
import com.studyagent.entity.StudentKnowledgeMastery;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.mapper.QuestionMapper;
import com.studyagent.mapper.StudentAnswerMapper;
import com.studyagent.mapper.StudentKnowledgeMasteryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final StudentKnowledgeMasteryMapper masteryMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final StudentService studentService;
    private final AsyncAnalysisService asyncAnalysisService;

    /**
     * 判断答案是否正确
     * 支持比较：
     * - "C" vs "C" -> true
     * - "C. cow" vs "C" -> true (提取选项字母比较)
     * - "C" vs "c" -> true (不区分大小写)
     */
    public static boolean isAnswerCorrect(String correctAnswer, String userAnswer) {
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }
        String correct = correctAnswer.trim();
        String answer = userAnswer.trim();

        // 直接相等（忽略大小写）
        if (correct.equalsIgnoreCase(answer)) {
            return true;
        }

        // 提取选项字母比较（例如 "C. cow" 和 "C" 都提取为 "C"）
        String correctLetter = extractOptionLetter(correct);
        String answerLetter = extractOptionLetter(answer);

        if (correctLetter != null && correctLetter.equalsIgnoreCase(answerLetter)) {
            return true;
        }

        return false;
    }

    /**
     * 从答案字符串中提取选项字母
     * 例如: "C. cow" -> "C", "A. apple" -> "A", "C" -> "C"
     */
    private static String extractOptionLetter(String answer) {
        if (answer == null || answer.isEmpty()) {
            return null;
        }
        answer = answer.trim();
        // 如果是 "X. xxx" 格式，提取第一个字符
        if (answer.length() >= 2 && answer.charAt(1) == '.') {
            return answer.substring(0, 1);
        }
        // 否则返回整个字符串
        return answer;
    }

    public Question create(String type, String content, String answer, String analysis,
                           Long subjectId, String knowledgePointIds, Integer difficulty) {
        return create(type, content, null, answer, analysis, subjectId, knowledgePointIds, difficulty);
    }

    public Question create(String type, String content, String options, String answer, String analysis,
                           Long subjectId, String knowledgePointIds, Integer difficulty) {
        Question q = new Question();
        q.setType(type != null ? type : "");
        q.setContent(content != null ? content : "");
        q.setOptions(options != null ? options : "");
        q.setAnswer(answer != null ? answer : "");
        q.setAnalysis(analysis != null ? analysis : "");
        q.setSubjectId(subjectId != null ? subjectId : 0L);
        q.setKnowledgePointIds(knowledgePointIds != null ? knowledgePointIds : "");
        q.setDifficulty(difficulty != null ? difficulty : 1);
        q.setFrequency(0);
        questionMapper.insert(q);
        return q;
    }

    public Question findById(Long id) {
        return questionMapper.selectById(id);
    }

    public List<Question> findBySubject(Long subjectId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getSubjectId, subjectId)
        );
    }

    public List<Question> findByKnowledgePoint(String kpIds) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .like(Question::getKnowledgePointIds, kpIds)
        );
    }

    @Transactional
    public StudentAnswer submitAnswer(Long studentId, Long questionId, String answer) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        boolean isCorrect = isAnswerCorrect(question.getAnswer(), answer);
        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setStudentId(studentId);
        studentAnswer.setQuestionId(questionId);
        studentAnswer.setAnswer(answer);
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswerMapper.insert(studentAnswer);

        if (!isCorrect) {
            if (question.getKnowledgePointIds() != null && !question.getKnowledgePointIds().isEmpty()) {
                try {
                    Long kpId = Long.parseLong(question.getKnowledgePointIds().split(",")[0]);
                    studentAnswer.setKnowledgePointId(kpId);
                    updateMastery(studentId, kpId, false);
                } catch (NumberFormatException e) {
                    log.warn("解析知识点ID失败: {}", question.getKnowledgePointIds(), e);
                }
            }
            
            log.info("提交异步分析任务，studentAnswerId: {}", studentAnswer.getId());
            asyncAnalysisService.analyzeWrongAnswerAsync(
                    studentAnswer.getId(),
                    question.getContent(),
                    answer,
                    question.getAnswer()
            );
        } else {
            studentService.addExpAndGold(studentId, 5, 10);
            if (question.getKnowledgePointIds() != null && !question.getKnowledgePointIds().isEmpty()) {
                try {
                    Long kpId = Long.parseLong(question.getKnowledgePointIds().split(",")[0]);
                    updateMastery(studentId, kpId, true);
                } catch (NumberFormatException e) {
                    log.warn("解析知识点ID失败: {}", question.getKnowledgePointIds(), e);
                }
            }
        }

        return studentAnswer;
    }

    private void updateMastery(Long studentId, Long kpId, boolean correct) {
        StudentKnowledgeMastery mastery = masteryMapper.selectOne(
                new LambdaQueryWrapper<StudentKnowledgeMastery>()
                        .eq(StudentKnowledgeMastery::getStudentId, studentId)
                        .eq(StudentKnowledgeMastery::getKnowledgePointId, kpId)
        );

        if (mastery == null) {
            mastery = new StudentKnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setKnowledgePointId(kpId);
            mastery.setMasteryLevel(0.0);
            mastery.setStarLevel(0);
            mastery.setPracticeCount(0);
            mastery.setCorrectCount(0);
        }

        mastery.setPracticeCount(mastery.getPracticeCount() + 1);
        if (correct) {
            mastery.setCorrectCount(mastery.getCorrectCount() + 1);
        }

        double rate = mastery.getPracticeCount() > 0
                ? (double) mastery.getCorrectCount() / mastery.getPracticeCount()
                : 0;
        mastery.setMasteryLevel(rate);
        mastery.setStarLevel(calculateStarLevel(rate));

        if (mastery.getId() == null) {
            masteryMapper.insert(mastery);
        } else {
            masteryMapper.updateById(mastery);
        }
    }

    private int calculateStarLevel(double rate) {
        if (rate >= 0.9) return 5;
        if (rate >= 0.8) return 4;
        if (rate >= 0.6) return 3;
        if (rate >= 0.4) return 2;
        if (rate >= 0.2) return 1;
        return 0;
    }

    public List<WrongAnswerResponse> getWrongAnswers(Long studentId) {
        List<StudentAnswer> answers = studentAnswerMapper.selectList(
                new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getStudentId, studentId)
                        .eq(StudentAnswer::getIsCorrect, false)
        );

        List<WrongAnswerResponse> responses = new ArrayList<>();
        for (StudentAnswer answer : answers) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            String kpName = null;
            if (answer.getKnowledgePointId() != null) {
                KnowledgePoint kp = knowledgePointMapper.selectById(answer.getKnowledgePointId());
                if (kp != null) {
                    kpName = kp.getName();
                }
            }
            responses.add(WrongAnswerResponse.builder()
                    .id(answer.getId())
                    .questionId(answer.getQuestionId())
                    .questionContent(question != null ? question.getContent() : null)
                    .questionType(question != null ? question.getType() : null)
                    .questionOptions(question != null ? question.getOptions() : null)
                    .correctAnswer(question != null ? question.getAnswer() : null)
                    .yourAnswer(answer.getAnswer())
                    .knowledgePointId(answer.getKnowledgePointId())
                    .knowledgePointName(kpName)
                    .createTime(answer.getCreateTime())
                    .build());
        }
        return responses;
    }
}