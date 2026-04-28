package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_api_key")
public class ApiKey {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String provider;
    private String apiKey;
    private String baiduOcrSecretKey;
    private String baseUrl;
    private String modelName;
    private Boolean enabled = true;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
