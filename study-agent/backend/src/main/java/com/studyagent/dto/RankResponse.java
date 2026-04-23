package com.studyagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankResponse {
    private Long studentId;
    private String nickname;
    private Integer level;
    private Integer totalGold;
    private Integer rank;
}
