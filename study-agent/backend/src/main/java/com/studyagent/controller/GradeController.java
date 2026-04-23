package com.studyagent.controller;

import com.studyagent.entity.Grade;
import com.studyagent.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
@CrossOrigin
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<Grade>> getAll() {
        return ResponseEntity.ok(gradeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> getById(@PathVariable Long id) {
        Grade grade = gradeService.findById(id);
        if (grade != null) {
            return ResponseEntity.ok(grade);
        }
        return ResponseEntity.notFound().build();
    }
}
