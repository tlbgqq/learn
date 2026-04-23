package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SprintHistoryResponse {
    private String sessionId;
    private String date;
    private String subjectName;
    private Integer totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private BigDecimal accuracy;
    private Integer maxCombo;
    private Integer duration;
}
