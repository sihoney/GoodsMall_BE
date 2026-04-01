package com.example.product.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger (SpringDoc OpenAPI) 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI productOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("상품 관리 서비스 API 문서\n\n" +
                                "**인증 헤더 필수:**\n" +
                                "- X-Member-Id: 회원 UUID (예: 550e8400-e29b-41d4-a716-446655440000)\n" +
                                "- X-Member-Role: USER, SELLER, ADMIN 중 하나\n" +
                                "- 기본적으로 생성되어 있는 상품 UUID (예: 75cf9d28-ae36-4d31-a3fc-75511aac1503)\n" +
                                "- 기본적으로 생성되어 있는 카테고리 UUID (예: c325102d-2853-42b8-a5f4-23cd7b9adcac)")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버 (직접 접근)")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
