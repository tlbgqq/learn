package com.studyagent.dto;

import lombok.Data;

@Data
public class SprintFinishRequest {
    private String sessionId;
    private Long studentId;
    private String reason;
}
