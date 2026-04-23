package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SprintAnswerResponse {
    private Boolean isCorrect;
    private String correctAnswer;
    private Integer comboCount;
    private Double timeBonus;
    private Double comboBonus;
    private Integer score;
    private RewardDto reward;
    private AchievementDto achievementUnlocked;
    private SprintQuestionResponse nextQuestion;

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
