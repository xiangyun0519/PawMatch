package com.petadopt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 配置。
 *
 * 访问地址：
 *  - JSON:  http://localhost:8080/v3/api-docs
 *  - UI:    http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI pawMatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PawMatch 流浪动物领养智能匹配平台 API")
                        .description("提供用户、宠物、救助站、AI 智能匹配、RAG 智能问答等接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PawMatch Team")
                                .email("support@pawmatch.local")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}