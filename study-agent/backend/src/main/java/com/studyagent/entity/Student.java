package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer gradeId;
    private Long classId;
    private Integer exp = 0;
    private Integer gold = 0;
    private Integer diamond = 0;
    private Integer level = 1;
    private Integer continuousStudyDays = 0;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
