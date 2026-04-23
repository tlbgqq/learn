package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_daily_task")
public class DailyTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String icon;
    private String description;
    private Integer targetCount;
    private Integer rewardGold = 0;
    private Integer rewardExp = 0;
    private String taskType;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
