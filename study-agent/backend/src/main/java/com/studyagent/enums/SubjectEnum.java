package com.studyagent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubjectEnum {

    CHINESE(1, "语文"),
    MATH(2, "数学"),
    ENGLISH(3, "英语");

    private final Integer code;
    private final String name;

    public static SubjectEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubjectEnum subject : values()) {
            if (subject.getCode().equals(code)) {
                return subject;
            }
        }
        return null;
    }

    public static SubjectEnum getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (SubjectEnum subject : values()) {
            if (subject.getName().equals(name)) {
                return subject;
            }
        }
        return null;
    }
}
