package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_student_daily_task")
public class StudentDailyTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long taskId;
    private Integer progress = 0;
    private Boolean completed = false;
    private LocalDateTime completedTime;
    private LocalDateTime date;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
