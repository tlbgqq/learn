package com.studyagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyagent.dto.ExamParseResult;
import com.studyagent.service.BaiduOcrService;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/image-parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> parseImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("Received image: name={}, size={}, type={}",
                    image.getOriginalFilename(),
                    image.getSize(),
                    image.getContentType());

            log.info("Step 1: OCR and structuring using glm-ocr...");
            String ocrText = baiduOcrService.recognizeText(image);
            log.info("OCR result length: {}, content preview: {}",
                    ocrText.length(), ocrText);

            log.info("Step 2: Parse glm-ocr response...");
            String structuredJson = extractJson(ocrText);
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

        emitter.onCompletion(() -> log.info("SSE stream completed"));
        emitter.onError(e -> log.error("SSE stream error: {}", e.getMessage()));
        emitter.onTimeout(() -> log.warn("SSE stream timeout"));

        new Thread(() -> {
            try {
                log.info("Stream: Received image: name={}, size={}", image.getOriginalFilename(), image.getSize());

                Map<String, Object> progress = new HashMap<>();
                progress.put("stage", "ocr");
                progress.put("message", "正在进行OCR识别和结构化分析...");
                emitter.send(SseEmitter.event().name("progress").data(objectMapper.writeValueAsString(progress)));

                String ocrText = baiduOcrService.recognizeText(image);
                log.info("Stream: OCR result length: {}， ocrText：{}", ocrText.length(), ocrText);

                String structuredJson = extractJson(ocrText);
                ExamParseResult examResult = null;
                try {
                    examResult = objectMapper.readValue(structuredJson, ExamParseResult.class);
                } catch (Exception e) {
                    log.warn("Stream: Failed to parse exam result: {}", e.getMessage());
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("fullText", ocrText);
                result.put("structuredJson", structuredJson);
                if (examResult != null) {
                    result.put("examResult", examResult);
                }

                emitter.send(SseEmitter.event().name("complete").data(objectMapper.writeValueAsString(result)));
                emitter.complete();

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
        result.put("ocr", "Ollama glm-ocr");
        result.put("ai", "无");
        result.put("message", "Ollama glm-ocr 直接识别并结构化");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ai-test")
    public ResponseEntity<Map<String, Object>> testAiConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "当前使用 Ollama glm-ocr，无需额外 AI 接口");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
}
