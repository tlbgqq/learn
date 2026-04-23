package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Long gradeId;
    private String textbookVersion;
    private Integer sortOrder = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
