package com.studyagent.ai;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionAnalysisService {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的K12教育AI助手。请分析学生的错题：

            题目内容：%s
            学生答案：%s
            正确答案：%s

            请以JSON格式返回分析结果：
            {
              "knowledge_point": "知识点名称",
              "error_type": "知识点缺失|理解偏差|审题错误|计算失误",
              "mastery_level": 0.0-1.0的数值,
              "practice_suggestion": "针对这个知识点的练习建议"
            }
            """;
    private final ChatModel chatModel;

    public AnalysisResult analyzeWrongAnswer(String questionContent, String studentAnswer, String correctAnswer) {
        try {
            String prompt = String.format(SYSTEM_PROMPT, questionContent, studentAnswer, correctAnswer);
            String response = chatModel.chat(prompt);
            return parseAnalysisResult(response);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return fallbackAnalysis(studentAnswer, correctAnswer);
        }
    }

    private AnalysisResult parseAnalysisResult(String response) {
        try {
            String json = extractJson(response);
            return new AnalysisResult(
                    getJsonValue(json, "knowledge_point", "未识别"),
                    getJsonValue(json, "error_type", "知识点缺失"),
                    Double.parseDouble(getJsonValue(json, "mastery_level", "0.3")),
                    getJsonValue(json, "practice_suggestion", "建议多练习相关题目")
            );
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            return new AnalysisResult("未识别", "知识点缺失", 0.3, "建议多练习");
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return "{}";
    }

    private String getJsonValue(String json, String key, String defaultValue) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return defaultValue;
    }

    private AnalysisResult fallbackAnalysis(String studentAnswer, String correctAnswer) {
        double similarity = calculateSimilarity(studentAnswer, correctAnswer);
        String errorType = similarity > 0.5 ? "理解偏差" : "知识点缺失";
        return new AnalysisResult("未识别", errorType, 0.3, "建议复习相关知识点后继续练习");
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public record AnalysisResult(
            String knowledgePoint,
            String errorType,
            double masteryLevel,
            String practiceSuggestion
    ) {}
}
