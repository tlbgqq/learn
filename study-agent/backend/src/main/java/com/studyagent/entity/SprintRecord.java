package com.studyagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sprint_record")
public class SprintRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;
    private Long studentId;
    private Long subjectId;
    private Integer totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracy;
    private Integer maxCombo;
    private Integer duration;
    private String endReason;
    private Boolean isNewRecord;

    @TableLogic
    private Integer del = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;
}
