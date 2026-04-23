package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sprint_session")
public class SprintSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private Long studentId;
    private Long subjectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String usedQuestionIds;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
