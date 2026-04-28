package com.studyagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.ExamParseResult;
import com.studyagent.service.ApiKeyService;
import com.studyagent.service.BaiduOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ImageParsingTestController {

    private final BaiduOcrService baiduOcrService;
    private final ApiKeyService apiKeyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/image-parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> parseImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("Received image: name={}, size={}, type={}",
                    image.getOriginalFilename(),
                    image.getSize(),
                    image.getContentType());

            // 1. OCR 识别图片文字（使用百度 OCR）
            log.info("Step 1: OCR recognition using Baidu OCR...");
            String ocrText = baiduOcrService.recognizeText(image);
            log.info("OCR result length: {}, content preview: {}",
                    ocrText.length(), ocrText);

            // 2. 发给 MiniMax AI 整理成结构化数据
            log.info("Step 2: Send to AI for structuring...");
            String structuredJson = sendToAi(ocrText);

            // 3. 解析 JSON 结果
            log.info("Step 3: Parse AI response...");
            ExamParseResult examResult = objectMapper.readValue(structuredJson, ExamParseResult.class);

            result.put("success", true);
            result.put("ocrText", ocrText);
            result.put("examResult", examResult);
            result.put("imageSize", image.getSize());
            result.put("imageName", image.getOriginalFilename());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Image parsing failed", e);
            result.put("success", false);
            result.put("error", e.getClass().getName() + ": " + e.getMessage());
            result.put("cause", e.getCause() != null ? e.getCause().getMessage() : "unknown");
            return ResponseEntity.badRequest().body(result);
        }
    }

    private String sendToAi(String ocrText) throws Exception {
        String apiKey = apiKeyService.getApiKeyValue("minimax")
                .orElseThrow(() -> new IllegalStateException("API key not found for minimax"));
        String baseUrl = apiKeyService.getBaseUrl("minimax")
                .orElse("https://api.minimaxi.com");

        // 获取模型名称
        String modelName = apiKeyService.getModelName("minimax").orElse("MiniMax-M2.7");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);

        String prompt = "请分析以下OCR识别的试卷内容，提取信息并以JSON格式返回。\n" +
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
                "      \"correctAnswer\": \"正确答案\",\n" +
                "      \"isCorrect\": true/false,\n" +
                "      \"knowledgePoints\": [\"知识点1\", \"知识点2\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "OCR识别内容：\n" + ocrText;

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", new Map[]{message});

        String url = baseUrl + "/messages";

        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        restTemplate.setRequestFactory(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Sending to AI: model={}, prompt length={}", modelName, prompt.length());
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        log.info("AI response: {}", response.getBody());

        // 解析 AI 返回的 content 中的 text
        String aiText = extractTextFromResponse(response.getBody());
        return aiText;
    }

    private String extractTextFromResponse(String responseBody) throws Exception {
        // 解析 {"content": [{"type": "text", "text": "..."}]}
        Map<String, Object> resp = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) resp.get("content");
        if (content != null && !content.isEmpty()) {
            for (Map<String, Object> item : content) {
                if ("text".equals(item.get("type"))) {
                    return (String) item.get("text");
                }
            }
        }
        throw new RuntimeException("无法从AI响应中提取文本");
    }

    @GetMapping("/vision-status")
    public ResponseEntity<Map<String, Object>> getVisionStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("supported", true);
        result.put("ocr", "Baidu OCR API (general_basic)");
        result.put("ai", "MiniMax-M2.7");
        result.put("message", "百度OCR识别 + AI结构化");
        return ResponseEntity.ok(result);
    }
}