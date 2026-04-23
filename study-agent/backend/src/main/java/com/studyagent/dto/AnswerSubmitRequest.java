package com.studyagent.dto;

import lombok.Data;

@Data
public class AnswerSubmitRequest {
    private Long studentId;
    private Long questionId;
    private String answer;
    private Long studentAnswerId;
}
