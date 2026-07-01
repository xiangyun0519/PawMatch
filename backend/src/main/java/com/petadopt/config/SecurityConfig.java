package com.petadopt.config;

import com.petadopt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置。
 *
 * 权限策略：
 *  - /api/auth/** /swagger-ui/** /v3/api-docs/**  完全公开
 *  - GET /api/pets/** /api/shelters/**  公开浏览
 *  - POST/PUT/DELETE /api/pets/** /api/shelters/**  需 SHELTER 或 ADMIN
 *  - /api/match/** /api/applications/my /api/adopter-profile/**  已登录
 *  - /api/applications/{id}/review /api/follow-ups/** /api/stats/platform  需 SHELTER 或 ADMIN
 *  - /api/knowledge/**  ADMIN 专属
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 公开：认证、工具
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/utils/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**").permitAll()

                        // 公开浏览：宠物/救助站 GET
                        .requestMatchers(HttpMethod.GET, "/api/pets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/shelters/**").permitAll()

                        // 角色控制：宠物/救助站写操作
                        .requestMatchers(HttpMethod.POST, "/api/pets/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pets/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pets/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shelters/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shelters/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shelters/**").hasAnyRole("SHELTER", "ADMIN")

                        // 角色控制：申请审核 / 回访 / 平台统计
                        .requestMatchers("/api/applications/*/review").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers("/api/follow-ups/**").hasAnyRole("SHELTER", "ADMIN")
                        .requestMatchers("/api/stats/platform").hasRole("ADMIN")
                        .requestMatchers("/api/stats/**").hasAnyRole("SHELTER", "ADMIN")

                        // ADMIN 专属：知识库管理
                        .requestMatchers("/api/knowledge/**").hasRole("ADMIN")

                        // 已登录：匹配、聊天、领养人画像、申请、救助站工作台
                        .requestMatchers("/api/match/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/api/adopter-profile/**").authenticated()
                        .requestMatchers("/api/applications/**").authenticated()

                        .anyRequest().authenticated()
                )
                // 注册 JWT filter：在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}