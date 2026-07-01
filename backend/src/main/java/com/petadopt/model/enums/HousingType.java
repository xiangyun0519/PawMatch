package com.petadopt.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 住房类型
 */
@Getter
@AllArgsConstructor
public enum HousingType {
    SMALL_APARTMENT("SMALL_APARTMENT", "小户型公寓"),
    LARGE_APARTMENT("LARGE_APARTMENT", "大户型公寓"),
    HOUSE_WITH_YARD("HOUSE_WITH_YARD", "带院子的房子"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    public static HousingType fromCode(String code) {
        if (code == null) return OTHER;
        for (HousingType h : values()) {
            if (h.code.equalsIgnoreCase(code)) return h;
        }
        return OTHER;
    }
}