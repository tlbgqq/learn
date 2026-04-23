package com.studyagent.controller;

import com.studyagent.ai.QuestionGenerationService;
import com.studyagent.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question/generate")
@RequiredArgsConstructor
@CrossOrigin
public class QuestionGenerationController {

    private final QuestionGenerationService questionGenerationService;

    @PostMapping("/{kpId}")
    public ResponseEntity<Map<String, Object>> generateForKnowledgePoint(@PathVariable Long kpId) {
        List<Question> questions = questionGenerationService.generateForKnowledgePoint(kpId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", questions.size());
        result.put("questions", questions);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> generateBatch() {
        int totalGenerated = questionGenerationService.generateForAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalGenerated", totalGenerated);
        result.put("message", "批量生成完成");
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("serviceAvailable", true);
        result.put("message", "题目生成服务就绪");
        
        return ResponseEntity.ok(result);
    }
}
