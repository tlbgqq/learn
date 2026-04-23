package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String type = "";
    private String content = "";
    private String options = "";
    private String answer = "";
    private String analysis = "";
    private Long subjectId = 0L;
    private String knowledgePointIds = "";
    private Integer difficulty = 1;
    private Integer frequency = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
