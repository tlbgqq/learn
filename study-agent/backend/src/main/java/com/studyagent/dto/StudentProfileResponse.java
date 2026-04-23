package com.studyagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentProfileResponse {
    private Long id;
    private String nickname;
    private String avatar;
    private Integer level;
    private Integer exp;
    private Integer gold;
    private Integer diamond;
    private Integer continuousStudyDays;
    private String gradeName;
}
