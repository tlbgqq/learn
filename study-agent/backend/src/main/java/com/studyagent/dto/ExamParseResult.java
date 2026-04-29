package com.studyagent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamParseResult {
    private String studentName;
    private String studentId;
    private String teacherName;
    private List<QuestionAnswer> questions;

    @Data
    public static class QuestionAnswer {
        private int questionNo;
        private String questionContent;

        private String options;
        private String studentAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        private List<String> knowledgePoints;
    }
}