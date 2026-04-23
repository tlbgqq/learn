package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_knowledge_point")
public class KnowledgePoint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String code;
    private Long parentId = 0L;
    private Long subjectId;
    private Long gradeId;
    private Integer difficulty = 1;
    private String description;
    private String commonErrors;
    private String relatedConcepts;
    private Integer sortOrder = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
