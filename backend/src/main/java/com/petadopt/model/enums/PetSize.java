package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 体型
 */
@Getter
@AllArgsConstructor
public enum PetSize {
    SMALL("SMALL", "小型"),
    MEDIUM("MEDIUM", "中型"),
    LARGE("LARGE", "大型");

    private final String code;
    private final String description;

    public static PetSize fromCode(String code) {
        if (code == null) return MEDIUM;
        for (PetSize s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        return MEDIUM;
    }
}