package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_student_answer")
public class StudentAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long questionId;
    private String answer;
    private Boolean isCorrect;
    private String errorType;
    private String aiAnalysis = "";
    private Long knowledgePointId;
    private Double masteryLevel;
    private Boolean isCorrected = false;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
