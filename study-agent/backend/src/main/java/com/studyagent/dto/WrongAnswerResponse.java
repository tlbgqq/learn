package com.studyagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongAnswerResponse {
    private Long id;
    private Long questionId;
    private String questionContent;
    private String questionType;
    private String questionOptions;
    private String correctAnswer;
    private String yourAnswer;
    private Long knowledgePointId;
    private String knowledgePointName;
    private LocalDateTime createTime;
}
