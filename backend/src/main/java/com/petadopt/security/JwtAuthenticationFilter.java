package com.petadopt.security;

import com.petadopt.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：从 Authorization 头解析 Bearer Token，
 * 把 userId / username / role 注入 Spring SecurityContext。
 *
 * 失败处理：仅记录日志，不主动拒绝（让后续 AuthorizationFilter 根据 URL 规则判断）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        try {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            if (role != null && !role.isBlank()) {
                // Spring Security 自动给 role 加 "ROLE_" 前缀；hasRole("ADOPTER") 匹配 ROLE_ADOPTER
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                auth.setDetails(userId);  // 把 userId 塞进 details，方便后续获取
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT 解析失败: {}", ex.getMessage());
            // 不主动 reject；交由后续过滤器或 Controller 自己处理（目前依赖注解/URL 规则）
        }

        filterChain.doFilter(request, response);
    }
}