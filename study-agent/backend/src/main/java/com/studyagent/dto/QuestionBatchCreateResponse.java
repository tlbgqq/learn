package com.studyagent.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionBatchCreateResponse {
    private Long parentQuestionId;
    private List<Long> childQuestionIds;
    private Integer totalCreated;
}
