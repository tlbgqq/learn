package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SprintStartResponse {
    private String sessionId;
    private String startTime;
    private Integer duration;
    private Long subjectId;
    private SprintQuestionResponse question;
}
