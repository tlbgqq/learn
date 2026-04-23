package com.studyagent.controller;

import com.studyagent.entity.KnowledgePoint;
import com.studyagent.entity.Subject;
import com.studyagent.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@CrossOrigin
public class KnowledgeController {

    private final KnowledgePointService knowledgePointService;

    @GetMapping("/subjects/{gradeId}")
    public ResponseEntity<List<Subject>> getSubjectsByGrade(@PathVariable Long gradeId) {
        return ResponseEntity.ok(knowledgePointService.findSubjectsByGrade(gradeId));
    }

    @GetMapping("/tree/{subjectId}")
    public ResponseEntity<List<KnowledgePoint>> getKnowledgeTree(@PathVariable Long subjectId) {
        return ResponseEntity.ok(knowledgePointService.findRootBySubject(subjectId));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<KnowledgePoint>> getChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(knowledgePointService.findChildren(parentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgePoint> getById(@PathVariable Long id) {
        KnowledgePoint kp = knowledgePointService.findById(id);
        if (kp != null) {
            return ResponseEntity.ok(kp);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<KnowledgePoint> create(@RequestBody KnowledgePoint knowledgePoint) {
        KnowledgePoint created = knowledgePointService.create(
                knowledgePoint.getName(),
                knowledgePoint.getCode(),
                knowledgePoint.getParentId(),
                knowledgePoint.getSubjectId(),
                knowledgePoint.getGradeId(),
                knowledgePoint.getDifficulty(),
                knowledgePoint.getDescription()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/subject/{subjectId}/grade/{gradeId}")
    public ResponseEntity<List<KnowledgePoint>> getBySubjectAndGrade(
            @PathVariable Long subjectId,
            @PathVariable Long gradeId) {
        return ResponseEntity.ok(knowledgePointService.findBySubjectAndGrade(subjectId, gradeId));
    }
}