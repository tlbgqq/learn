package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SprintBestResponse {
    private Integer bestScore;
    private Integer bestCombo;
    private Double bestAccuracy;
    private Integer totalSessions;
    private Integer totalQuestions;
    private Integer totalCorrect;
}
