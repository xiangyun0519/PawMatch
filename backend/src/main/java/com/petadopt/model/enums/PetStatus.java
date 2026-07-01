package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 宠物状态
 */
@Getter
@AllArgsConstructor
public enum PetStatus {
    AVAILABLE("AVAILABLE", "可领养"),
    PENDING("PENDING", "审核中"),
    ADOPTED("ADOPTED", "已领养"),
    UNAVAILABLE("UNAVAILABLE", "不可领养");

    private final String code;
    private final String description;

    public static PetStatus fromCode(String code) {
        if (code == null) return AVAILABLE;
        for (PetStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        return AVAILABLE;
    }
}