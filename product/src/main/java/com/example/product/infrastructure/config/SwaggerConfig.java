package com.example.product.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                                "- X-Member-Role: USER, SELLER, ADMIN 중 하나")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버")
                ));
    }
}
