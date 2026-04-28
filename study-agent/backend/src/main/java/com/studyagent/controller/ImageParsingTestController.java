package com.studyagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.ExamParseResult;
import com.studyagent.service.BaiduOcrService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatResponseHandler;
import dev.langchain4j.model.output.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    private final StreamingChatModel streamingChatModel;
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

    @PostMapping(value = "/image-parse-stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter parseImageStream(@RequestParam("image") MultipartFile image) {
        SseEmitter emitter = new SseEmitter(300000L);
        StringBuilder fullResponse = new StringBuilder();

        emitter.onCompletion(() -> log.info("SSE stream completed"));
        emitter.onError(e -> log.error("SSE stream error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE stream timeout"));

        new Thread(() -> {
            try {
                log.info("Stream: Received image: name={}, size={}", image.getOriginalFilename(), image.getSize());

                Map<String, Object> progress = new HashMap<>();
                progress.put("stage", "ocr");
                progress.put("message", "正在进行OCR识别...");
                emitter.send(SseEmitter.event().name("progress").data(objectMapper.writeValueAsString(progress)));

                String ocrText = baiduOcrService.recognizeText(image);
                log.info("Stream: OCR result length: {}， ocrText：{}", ocrText.length(), ocrText);

                Map<String, Object> ocrResult = new HashMap<>();
                ocrResult.put("stage", "ocr_done");
                ocrResult.put("message", "OCR识别完成");
                ocrResult.put("ocrText", ocrText);
                emitter.send(SseEmitter.event().name("progress").data(objectMapper.writeValueAsString(ocrResult)));

                Map<String, Object> aiProgress = new HashMap<>();
                aiProgress.put("stage", "ai");
                aiProgress.put("message", "AI正在分析，请稍候...");
                emitter.send(SseEmitter.event().name("progress").data(objectMapper.writeValueAsString(aiProgress)));

                sendToAiStream(ocrText, emitter, fullResponse);

            } catch (Exception e) {
                log.error("Stream: Image parsing failed", e);
                try {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", e.getClass().getName() + ": " + e.getMessage());
                    emitter.send(SseEmitter.event().name("error").data(objectMapper.writeValueAsString(error)));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
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
                "      \"options\": \"选择题的选项\",\n" +
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

    private void sendToAiStream(String ocrText, SseEmitter emitter, StringBuilder fullResponse) {
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
                "      \"options\": \"选择题的选项\",\n" +
                "      \"correctAnswer\": \"正确答案\",\n" +
                "      \"isCorrect\": true/false,\n" +
                "      \"knowledgePoints\": [\"知识点1\", \"知识点2\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "OCR识别内容：\n" + ocrText;

        log.info("Stream: Sending to AI: prompt length={}", prompt.length());

        streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    fullResponse.append(partialResponse);
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", partialResponse);
                    data.put("fullText", fullResponse.toString());
                    emitter.send(SseEmitter.event().name("token").data(objectMapper.writeValueAsString(data)));
                } catch (IOException e) {
                    log.error("Stream: Error sending token: {}", e.getMessage());
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                try {
                    String finalResponse = fullResponse.toString();
                    log.info("Stream: AI response completed, length={}", finalResponse.length());
                    String structuredJson = extractJson(finalResponse);

                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("fullText", finalResponse);
                    result.put("structuredJson", structuredJson);

                    try {
                        ExamParseResult examResult = objectMapper.readValue(structuredJson, ExamParseResult.class);
                        result.put("examResult", examResult);
                    } catch (Exception e) {
                        log.warn("Stream: Failed to parse exam result: {}", e.getMessage());
                    }

                    emitter.send(SseEmitter.event().name("complete").data(objectMapper.writeValueAsString(result)));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("Stream: Error completing response: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Stream: AI chat error", error);
                try {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("error", error.getClass().getName() + ": " + error.getMessage());
                    emitter.send(SseEmitter.event().name("error").data(objectMapper.writeValueAsString(err)));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(error);
            }
        });
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

    @GetMapping("/ai-test")
    public ResponseEntity<Map<String, Object>> testAiConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("Testing AI connection...");
            String testPrompt = "请回复'连接成功'四个字，不要回复其他内容。";
            String response = chatModel.chat(testPrompt);
            log.info("AI test response: {}", response);

            result.put("success", true);
            result.put("message", "AI接口连接正常");
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI connection test failed", e);
            result.put("success", false);
            result.put("message", "AI接口连接失败");
            result.put("error", e.getClass().getName() + ": " + e.getMessage());
            result.put("cause", e.getCause() != null ? e.getCause().getMessage() : "unknown");
            return ResponseEntity.status(500).body(result);
        }
    }
}
