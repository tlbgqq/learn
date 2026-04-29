package com.studyagent.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BaiduOcrService {

    private static final String PROVIDER_NAME = "baidu-ocr";

    private final OkHttpClient httpClient = new OkHttpClient().newBuilder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    @Value("${ollama.api-url:http://localhost:11434/api/generate}")
    private String ollamaApiUrl;

    @Value("${ollama.model:glm-ocr:latest}")
    private String ollamaModel;

    public BaiduOcrService() {
    }

    public String recognizeText(MultipartFile image) {
        try {
            log.info("Using Ollama glm-ocr for image: name={}, size={}",
                    image.getOriginalFilename(), image.getSize());

            String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());

            String prompt = "请分析以下图片中的试卷内容，提取信息并以JSON格式返回。\n" +
                    "JSON格式：\n" +
                    "{\n" +
                    "  \"studentName\": \"学生姓名\",\n" +
                    "  \"studentId\": \"学号\",\n" +
                    "  \"teacherName\": \"老师姓名\",\n" +
                    "  \"questions\": [\n" +
                    "    {\n" +
                    "      \"questionNo\": 1,\n" +
                    "      \"questionContent\": \"题目内容\",\n" +
                    "      \"studentAnswer\": \"学生答案\",\n" +
                    "      \"options\": \"选择题的选项\",\n" +
                    "      \"correctAnswer\": \"正确答案\",\n" +
                    "      \"isCorrect\": true/false,\n" +
                    "      \"knowledgePoints\": [\"知识点1\", \"知识点2\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "要求：\n" +
                    "1. 只返回JSON格式的结果，不要包含任何其他内容\n" +
                    "2. 如果某些字段无法提取，请留空字符串或null\n" +
                    "3. 对于isCorrect字段，如果无法判断，请设置为null\n" +
                    "4. 忽略涉及图片的题目";

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("images", new java.util.ArrayList<>(java.util.Arrays.asList(imageBase64)));
            requestBody.put("stream", false);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBody.toString());

            Request request = new Request.Builder()
                    .url(ollamaApiUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Ollama API request failed: " + response.code());
                }

                String responseBody = response.body().string();
                log.info("Ollama API response received");

                JSONObject json = new JSONObject(responseBody);

                String result = extractTextFromResponse(json);
                log.info("Ollama glm-ocr completed, text length: {}", result.length());
                
                return result;
            }

        } catch (IOException e) {
            log.error("Ollama glm-ocr processing failed", e);
            throw new RuntimeException("Ollama glm-ocr处理失败: " + e.getMessage());
        }
    }

    private String extractTextFromResponse(JSONObject json) {
        if (json.has("response")) {
            return json.getString("response").trim();
        }
        return "";
    }
}
