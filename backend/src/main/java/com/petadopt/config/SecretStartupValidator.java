package com.petadopt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SecretStartupValidator implements CommandLineRunner {

    // 启动校验：检测是否使用了历史硬编码占位符（历史 bug 残留）。命中即启动失败，强制从环境变量注入。
    private static final String PLACEHOLDER_OPENAI = "PLACEHOLDER_REPLACE_ME";
    private static final String PLACEHOLDER_DASHSCOPE = "PLACEHOLDER_REPLACE_ME";
    private static final String PLACEHOLDER_JWT = "your-jwt-secret-key-at-least-256-bits-long";
    private static final String PLACEHOLDER_MARKER = "__REPLACE_ME__";

    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;

    @Value("${spring.ai.dashscope.api-key:}")
    private String dashscopeKey;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Override
    public void run(String... args) {
        if (isPlaceholder(openaiKey, PLACEHOLDER_OPENAI)) {
            throw new IllegalStateException(
                "spring.ai.openai.api-key 仍使用仓库占位值，请通过环境变量 OPENAI_API_KEY 注入真实 key");
        }
        if (isPlaceholder(dashscopeKey, PLACEHOLDER_DASHSCOPE)) {
            throw new IllegalStateException(
                "spring.ai.dashscope.api-key 仍使用仓库占位值，请通过环境变量 DASHSCOPE_API_KEY 注入真实 key");
        }
        if (isPlaceholder(jwtSecret, PLACEHOLDER_JWT) || isPlaceholder(jwtSecret, PLACEHOLDER_MARKER)) {
            throw new IllegalStateException(
                "jwt.secret 仍使用默认值，请通过环境变量 JWT_SECRET 注入 ≥256 bit 随机串");
        }
        if (jwtSecret != null && jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "jwt.secret 长度不足 256 bit（至少 32 字符），当前长度: " + jwtSecret.length());
        }
    }

    private boolean isPlaceholder(String value, String placeholder) {
        return value != null && value.equals(placeholder);
    }
}