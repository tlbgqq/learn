package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SprintResultResponse {
    private String sessionId;
    private Integer totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Double accuracy;
    private Integer maxCombo;
    private Integer duration;
    private RewardDto rewards;
    private List<AchievementDto> newAchievements;
    private Boolean isNewRecord;
    private Integer previousBest;
    private Integer historyRank;

    @Data
    @Builder
    public static class RewardDto {
        private Integer gold;
        private Integer exp;
        private Integer diamond;
    }

    @Data
    @Builder
    public static class AchievementDto {
        private Long id;
        private String name;
        private String icon;
    }
}
