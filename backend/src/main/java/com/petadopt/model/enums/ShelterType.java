package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 救助站类型
 */
@Getter
@AllArgsConstructor
public enum ShelterType {
    GOVERNMENT("GOVERNMENT", "政府救助站"),
    NGO("NGO", "民间公益"),
    PERSONAL("PERSONAL", "个人救助"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    public static ShelterType fromCode(String code) {
        if (code == null) return OTHER;
        for (ShelterType t : values()) {
            if (t.code.equalsIgnoreCase(code)) return t;
        }
        return OTHER;
    }
}