package com.studyagent.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Question;
import com.studyagent.mapper.KnowledgePointMapper;
import com.studyagent.service.QuestionService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationService {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的K12教育题目生成专家。请根据以下知识点信息，生成5道练习题。

            知识点信息：
            - 名称：%s
            - 描述：%s
            - 难度等级：%d星（1为最简单，5为最难）
            - 常见错误：%s
            - 关联概念：%s

            题目要求：
            1. 选择题2道、填空题2道、解答题1道
            2. 难度与知识点难度一致
            3. 答案准确，解析清晰，解析要包含解题思路和关键知识点
            4. 选择题必须有4个选项（A、B、C、D），且只有一个正确答案
            5. 填空题的答案要具体明确，不要模糊
            6. 题目用中文

            请严格以JSON数组格式返回，不要有其他文字说明，题目结构：
            [
              {"type": "选择", "content": "题干", "options": ["A. 选项1","B. 选项2","C. 选项3","D. 选项4"], "answer": "B", "analysis": "详细解析..."},
              {"type": "填空", "content": "题干，缺少____部分", "options": [], "answer": "答案内容", "analysis": "详细解析..."},
              {"type": "解答", "content": "题干", "options": [], "answer": "解答过程和答案", "analysis": "详细解析..."}
            ]
            """;
    private final ChatModel chatModel;
    private final QuestionService questionService;
    private final KnowledgePointMapper knowledgePointMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public List<Question> generateForKnowledgePoint(Long knowledgePointId) {
        KnowledgePoint kp = knowledgePointMapper.selectById(knowledgePointId);
        if (kp == null) {
            log.error("知识点不存在: {}", knowledgePointId);
            return new ArrayList<>();
        }
        return generateForKnowledgePoint(kp);
    }

    @Transactional
    public List<Question> generateForKnowledgePoint(KnowledgePoint kp) {
        List<Question> generatedQuestions = new ArrayList<>();

        try {
            String description = kp.getDescription() != null ? kp.getDescription() : "";
            String commonErrors = kp.getCommonErrors() != null ? kp.getCommonErrors() : "无";
            String relatedConcepts = kp.getRelatedConcepts() != null ? kp.getRelatedConcepts() : "无";

            String prompt = String.format(SYSTEM_PROMPT,
                    kp.getName(),
                    description,
                    kp.getDifficulty(),
                    commonErrors,
                    relatedConcepts
            );

            log.info("开始为知识点 {} 生成题目", kp.getName());
            String response = chatModel.chat(prompt);
            log.info("AI 响应: {}", response);

            List<GeneratedQuestion> generatedQuestionList = parseQuestions(response);
            log.info("解析到 {} 道题目", generatedQuestionList.size());

            for (GeneratedQuestion gq : generatedQuestionList) {
                try {
                    String optionsStr = null;
                    if (gq.options() != null && !gq.options().isEmpty()) {
                        optionsStr = objectMapper.writeValueAsString(gq.options());
                    }

                    Question question = questionService.create(
                            gq.type(),
                            gq.content(),
                            optionsStr,
                            gq.answer(),
                            gq.analysis(),
                            kp.getSubjectId(),
                            String.valueOf(kp.getId()),
                            kp.getDifficulty()
                    );

                    generatedQuestions.add(question);
                    log.info("题目已保存: {} - {}", question.getType(), question.getContent().substring(0, Math.min(30, question.getContent().length())));
                } catch (Exception e) {
                    log.error("保存题目失败", e);
                }
            }

        } catch (Exception e) {
            log.error("为知识点 {} 生成题目失败", kp.getName(), e);
        }

        return generatedQuestions;
    }

    public int generateForAll() {
        List<KnowledgePoint> allPoints = knowledgePointMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
        );

        int totalGenerated = 0;
        for (KnowledgePoint kp : allPoints) {
            try {
                List<Question> questions = generateForKnowledgePoint(kp);
                totalGenerated += questions.size();
                log.info("知识点 {} 生成了 {} 道题目", kp.getName(), questions.size());
                
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("批量生成时处理知识点 {} 失败", kp.getName(), e);
            }
        }

        return totalGenerated;
    }

    private List<GeneratedQuestion> parseQuestions(String response) {
        List<GeneratedQuestion> result = new ArrayList<>();
        try {
            String jsonArray = extractJsonArray(response);
            log.debug("提取的JSON数组: {}", jsonArray);

            result = objectMapper.readValue(jsonArray, new TypeReference<List<GeneratedQuestion>>() {});
        } catch (Exception e) {
            log.error("使用 Jackson 解析失败，尝试使用正则解析", e);
            result = parseQuestionsWithRegex(response);
        }
        return result;
    }

    private String extractJsonArray(String response) {
        int start = response.indexOf("[");
        int end = response.lastIndexOf("]");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return "[]";
    }

    private List<GeneratedQuestion> parseQuestionsWithRegex(String response) {
        List<GeneratedQuestion> result = new ArrayList<>();
        String jsonArray = extractJsonArray(response);

        Pattern questionPattern = Pattern.compile("\\{[^}]+\"type\"[^}]+\\}");
        Matcher matcher = questionPattern.matcher(jsonArray);

        while (matcher.find()) {
            String questionJson = matcher.group();
            try {
                String type = getJsonValue(questionJson, "type", "选择");
                String content = getJsonValue(questionJson, "content", "");
                String answer = getJsonValue(questionJson, "answer", "");
                String analysis = getJsonValue(questionJson, "analysis", "");
                List<String> options = parseOptions(questionJson);

                result.add(new GeneratedQuestion(type, content, options, answer, analysis));
            } catch (Exception e) {
                log.error("正则解析题目失败: {}", questionJson, e);
            }
        }

        return result;
    }

    private List<String> parseOptions(String questionJson) {
        List<String> options = new ArrayList<>();
        Pattern optionsPattern = Pattern.compile("\"options\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = optionsPattern.matcher(questionJson);

        if (matcher.find()) {
            String optionsContent = matcher.group(1);
            Pattern optionPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher optionMatcher = optionPattern.matcher(optionsContent);
            while (optionMatcher.find()) {
                options.add(optionMatcher.group(1));
            }
        }

        return options;
    }

    private String getJsonValue(String json, String key, String defaultValue) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return defaultValue;
    }

    public record GeneratedQuestion(
            String type,
            String content,
            List<String> options,
            String answer,
            String analysis
    ) {}
}
