package com.petadopt.service;

import java.lang.reflect.Field;

/**
 * 测试工具：反射给 private @Value 字段赋值。
 */
public final class TestUtils {

    private TestUtils() {}

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}