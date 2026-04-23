package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_student_knowledge_mastery")
public class StudentKnowledgeMastery {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long knowledgePointId;
    private Double masteryLevel = 0.0;
    private Integer starLevel = 0;
    private Integer practiceCount = 0;
    private Integer correctCount = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
