package com.studyagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KnowledgeMasteryResponse {
    private List<KnowledgeMastery> knowledgePoints;

    @Data
    @AllArgsConstructor
    public static class KnowledgeMastery {
        private Long id;
        private String name;
        private Double masteryLevel;
        private Integer starLevel;
    }
}
