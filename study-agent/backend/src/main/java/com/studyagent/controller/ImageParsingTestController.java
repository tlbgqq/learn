package com.studyagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.ExamParseResult;
import com.studyagent.service.BaiduOcrService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ImageParsingTestController {

    private final BaiduOcrService baiduOcrService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/image-parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> parseImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("Received image: name={}, size={}, type={}",
                    image.getOriginalFilename(),
                    image.getSize(),
                    image.getContentType());

            log.info("Step 1: OCR recognition using Baidu OCR...");
            String ocrText = baiduOcrService.recognizeText(image);
            log.info("OCR result length: {}, content preview: {}",
                    ocrText.length(), ocrText);

            log.info("Step 2: Send to AI for structuring...");
            String structuredJson = sendToAi(ocrText);

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

    private String sendToAi(String ocrText) {
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

        log.info("Sending to AI: prompt length={}", prompt.length());
        String response = chatModel.chat(prompt);
        log.info("AI response: {}", response);

        return extractJson(response);
    }

    private String extractJson(String response) {
        if (response == null) {
            return "{}";
        }
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    @GetMapping("/vision-status")
    public ResponseEntity<Map<String, Object>> getVisionStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("supported", true);
        result.put("ocr", "Baidu OCR API (general_basic)");
        result.put("ai", "MiniMax-M2.7 (via LangChain4j)");
        result.put("message", "百度OCR识别 + AI结构化");
        return ResponseEntity.ok(result);
    }
}
