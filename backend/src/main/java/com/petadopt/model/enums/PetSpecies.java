package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物种
 */
@Getter
@AllArgsConstructor
public enum PetSpecies {
    DOG("DOG", "狗"),
    CAT("CAT", "猫"),
    RABBIT("RABBIT", "兔"),
    BIRD("BIRD", "鸟"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    public static PetSpecies fromCode(String code) {
        if (code == null) return OTHER;
        for (PetSpecies s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        return OTHER;
    }
}