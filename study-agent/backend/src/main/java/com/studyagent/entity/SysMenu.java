package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private String name;
    private Integer type;
    private String path;
    private String icon;
    private Integer sort;
    private String permission;
    private Integer isShow;
    private Integer isEnable;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
