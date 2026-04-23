package com.studyagent.dto;

import lombok.Data;

@Data
public class SprintAnswerRequest {
    private String sessionId;
    private Long studentId;
    private Long questionId;
    private String answer;
    private Double answerTime;
}
