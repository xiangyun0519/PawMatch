package com.petadopt.util;

import java.lang.reflect.Field;

public final class TestUtils {
    private TestUtils() {}

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new RuntimeException("Field not found: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field " + fieldName, e);
        }
    }
}