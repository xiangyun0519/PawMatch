package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    ADOPTER("ADOPTER", "领养人"),
    SHELTER("SHELTER", "救助站"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static UserRole fromCode(String code) {
        if (code == null) return ADOPTER;
        for (UserRole r : values()) {
            if (r.code.equalsIgnoreCase(code)) return r;
        }
        return ADOPTER;
    }
}