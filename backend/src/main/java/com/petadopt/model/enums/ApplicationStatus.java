package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 领养申请状态
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatus {
    PENDING("PENDING", "待审核"),
    REVIEWING("REVIEWING", "审核中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    public static ApplicationStatus fromCode(String code) {
        if (code == null) return PENDING;
        for (ApplicationStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        return PENDING;
    }
}