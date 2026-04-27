package com.studyagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.ExamParseResult;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ImageParsingTestController {

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

            // 1. 将图片转换为 Base64 编码
            log.info("Step 1: Convert image to Base64...");
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
            log.info("Image converted to Base64, length: {}", base64Image.length());

            // 2. 使用 langchain4j 直接调用大模型进行图像识别和结构化
            log.info("Step 2: Send image to AI for vision recognition and structuring...");
            String structuredJson = sendImageToAi(base64Image, mimeType);

            // 3. 解析 JSON 结果
            log.info("Step 3: Parse AI response...");
            ExamParseResult examResult = objectMapper.readValue(structuredJson, ExamParseResult.class);

            result.put("success", true);
            result.put("ocrText", "Vision AI 直接识别（使用视觉模型");
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

    private String sendImageToAi(String base64Image, String mimeType) throws Exception {
        String prompt = "请分析这张试卷图片，提取信息并以JSON格式返回。\n" +
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
                "请直接返回JSON格式的结果，不要包含任何其他说明文字。";

        UserMessage userMessage = UserMessage.from(
                ImageContent.from(base64Image, mimeType),
                TextContent.from(prompt)
        );

        log.info("Sending image to AI: using ChatModel (langchain4j)");
        ChatResponse response = chatModel.chat(userMessage);

        String aiText = response.aiMessage().text();
        log.info("AI response: {}", aiText);

        return aiText;
    }

    @GetMapping("/vision-status")
    public ResponseEntity<Map<String, Object>> getVisionStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("supported", true);
        result.put("ocr", "Vision AI (直接使用视觉模型识别)");
        result.put("ai", "LangChain4j + 视觉模型");
        result.put("message", "直接使用视觉模型识别图片 + AI结构化");
        result.put("configSource", "t_api_key 表");
        return ResponseEntity.ok(result);
    }
}