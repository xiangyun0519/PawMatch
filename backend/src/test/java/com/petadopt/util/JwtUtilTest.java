package com.petadopt.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        TestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-256-bits-long-padding");
        TestUtils.setField(jwtUtil, "expiration", 86400L);
    }

    @Test
    @DisplayName("生成与解析 token：userId/username/role 一致")
    void generate_and_parse() {
        String token = jwtUtil.generateToken(100L, "alice", "ADOPTER");
        assertThat(token).isNotBlank();

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(100L);
        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("role", String.class)).isEqualTo("ADOPTER");
        assertThat(jwtUtil.getUserId(token)).isEqualTo(100L);
        assertThat(jwtUtil.getUsername(token)).isEqualTo("alice");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ADOPTER");
    }

    @Test
    @DisplayName("validateToken：未过期返回 true")
    void validate_valid_token() {
        String token = jwtUtil.generateToken(1L, "u", "ADOPTER");
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("篡改 token → 解析失败")
    void tampered_token_throws() {
        String token = jwtUtil.generateToken(1L, "u", "ADOPTER");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertThatThrownBy(() -> jwtUtil.parseToken(tampered)).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("过期 token → 解析时抛 ExpiredJwtException")
    void expired_token() {
        TestUtils.setField(jwtUtil, "expiration", -1L);
        String token = jwtUtil.generateToken(1L, "u", "ADOPTER");
        assertThatThrownBy(() -> jwtUtil.parseToken(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}