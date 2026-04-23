package com.studyagent.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SprintQuestionResponse {
    private Long id;
    private String type;
    private String content;
    private List<String> options;
    private Integer difficulty;
}
