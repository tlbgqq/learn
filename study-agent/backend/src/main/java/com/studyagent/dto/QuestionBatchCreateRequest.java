package com.studyagent.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionBatchCreateRequest {
    private QuestionDTO parentQuestion;
    private List<QuestionDTO> childQuestions;

    @Data
    public static class QuestionDTO {
        private String type;
        private String content;
        private String options;
        private String answer;
        private String analysis;
        private Long subjectId;
        private Long gradeId;
        private String knowledgePointIds;
        private Integer difficulty;
    }
}
