package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动水平
 */
@Getter
@AllArgsConstructor
public enum ActivityLevel {
    LOW("LOW", "低"),
    MODERATE("MODERATE", "中"),
    HIGH("HIGH", "高");

    private final String code;
    private final String description;

    public static ActivityLevel fromCode(String code) {
        if (code == null) return MODERATE;
        for (ActivityLevel a : values()) {
            if (a.code.equalsIgnoreCase(code)) return a;
        }
        return MODERATE;
    }
}