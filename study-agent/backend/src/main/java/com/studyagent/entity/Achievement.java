package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String icon;
    private String description;
    private String conditionType;
    private Integer conditionValue;
    private Integer rewardGold = 0;
    private Integer rewardExp = 0;
    private Integer rewardDiamond = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
