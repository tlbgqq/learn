package com.studyagent.controller;

import com.studyagent.entity.Subject;
import com.studyagent.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject")
@RequiredArgsConstructor
@CrossOrigin
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getById(@PathVariable Long id) {
        Subject subject = subjectService.findById(id);
        if (subject != null) {
            return ResponseEntity.ok(subject);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/grade/{gradeId}")
    public ResponseEntity<List<Subject>> getByGradeId(@PathVariable Long gradeId) {
        return ResponseEntity.ok(subjectService.findByGradeId(gradeId));
    }
}
