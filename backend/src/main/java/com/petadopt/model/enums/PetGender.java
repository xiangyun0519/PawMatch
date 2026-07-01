package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别
 */
@Getter
@AllArgsConstructor
public enum PetGender {
    MALE("MALE", "公"),
    FEMALE("FEMALE", "母");

    private final String code;
    private final String description;

    public static PetGender fromCode(String code) {
        if (code == null) return MALE;
        for (PetGender g : values()) {
            if (g.code.equalsIgnoreCase(code)) return g;
        }
        return MALE;
    }
}