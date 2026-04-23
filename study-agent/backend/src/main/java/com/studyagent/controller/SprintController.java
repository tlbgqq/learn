package com.studyagent.controller;

import com.studyagent.config.JwtInterceptor;
import com.studyagent.dto.*;
import com.studyagent.service.SprintService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice/sprint")
@RequiredArgsConstructor
@CrossOrigin
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/start")
    public ResponseEntity<SprintStartResponse> startSprint(
            @RequestBody SprintStartRequest request,
            HttpServletRequest httpRequest) {
        Long studentId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_HEADER);
        SprintStartResponse response = sprintService.startSprint(
                studentId,
                request.getSubjectId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{sessionId}")
    public ResponseEntity<SprintQuestionResponse> getNextQuestion(@PathVariable String sessionId) {
        SprintQuestionResponse response = sprintService.getNextQuestion(sessionId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/answer")
    public ResponseEntity<SprintAnswerResponse> submitAnswer(
            @RequestBody SprintAnswerRequest request,
            HttpServletRequest httpRequest) {
        Long studentId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_HEADER);
        SprintAnswerResponse response = sprintService.submitAnswer(
                request.getSessionId(),
                studentId,
                request.getQuestionId(),
                request.getAnswer(),
                request.getAnswerTime()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish")
    public ResponseEntity<SprintResultResponse> finishSprint(
            @RequestBody SprintFinishRequest request,
            HttpServletRequest httpRequest) {
        Long studentId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_HEADER);
        SprintResultResponse response = sprintService.finishSprint(
                request.getSessionId(),
                studentId,
                request.getReason()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SprintHistoryResponse>> getHistory(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) Integer limit) {
        Long studentId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_HEADER);
        List<SprintHistoryResponse> history = sprintService.getHistory(studentId, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/best")
    public ResponseEntity<SprintBestResponse> getBestRecord(HttpServletRequest httpRequest) {
        Long studentId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_HEADER);
        SprintBestResponse best = sprintService.getBestRecord(studentId);
        return ResponseEntity.ok(best);
    }
}
