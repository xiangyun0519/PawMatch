package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 养宠经验
 */
@Getter
@AllArgsConstructor
public enum PetExperience {
    NONE("NONE", "无经验"),
    BEGINNER("BEGINNER", "新手"),
    INTERMEDIATE("INTERMEDIATE", "有一定经验"),
    EXPERT("EXPERT", "经验丰富");

    private final String code;
    private final String description;

    public static PetExperience fromCode(String code) {
        if (code == null) return NONE;
        for (PetExperience e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return NONE;
    }
}