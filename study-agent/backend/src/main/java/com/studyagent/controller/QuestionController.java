package com.studyagent.controller;

import com.studyagent.dto.AnswerSubmitRequest;
import com.studyagent.dto.WrongAnswerResponse;
import com.studyagent.entity.Question;
import com.studyagent.entity.StudentAnswer;
import com.studyagent.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
@CrossOrigin
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<Question> create(@RequestBody Question question) {
        Question created = questionService.create(
                question.getType(),
                question.getContent(),
                question.getAnswer(),
                question.getAnalysis(),
                question.getSubjectId(),
                question.getKnowledgePointIds(),
                question.getDifficulty()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getById(@PathVariable Long id) {
        Question question = questionService.findById(id);
        if (question != null) {
            return ResponseEntity.ok(question);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<Question>> getBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(questionService.findBySubject(subjectId));
    }

    @GetMapping("/knowledge/{kpIds}")
    public ResponseEntity<List<Question>> getByKnowledgePoint(@PathVariable String kpIds) {
        return ResponseEntity.ok(questionService.findByKnowledgePoint(kpIds));
    }

    @PostMapping("/submit")
    public ResponseEntity<StudentAnswer> submitAnswer(@RequestBody AnswerSubmitRequest request) {
        StudentAnswer answer = questionService.submitAnswer(
                request.getStudentId(),
                request.getQuestionId(),
                request.getAnswer()
        );
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/wrong/{studentId}")
    public ResponseEntity<List<WrongAnswerResponse>> getWrongAnswers(@PathVariable Long studentId) {
        return ResponseEntity.ok(questionService.getWrongAnswers(studentId));
    }
}