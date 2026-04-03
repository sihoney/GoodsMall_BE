package com.example.payment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * payment 모듈의 API 문서 메타데이터를 구성하고,
     * Gateway Swagger UI에서 Bearer 토큰을 입력받아 호출할 수 있도록 문서 보안 스키마를 구성한다.
     * servers를 "/"(상대경로)로 고정하여 Swagger UI가 열린 origin(gateway) 기준으로 API를 호출하도록 한다.
     */
    @Bean
    public OpenAPI paymentOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("member-service 로그인으로 받은 accessToken을 입력하면 gateway가 X-Member-Id / X-Member-Role 헤더를 내부적으로 주입합니다.");

        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("v1")
                        .description("payment service API documentation\n\n" +
                                "**권장 테스트 순서**\n" +
                                "1. member-service `POST /api/auth/login` 호출\n" +
                                "2. 응답의 `accessToken` 복사\n" +
                                "3. payment-service 우측 상단 Authorize에 `Bearer <accessToken>` 입력\n\n" +
                                "**buyer seed 계정**\n" +
                                "- email: `buyer@test.local`\n" +
                                "- password: `test1234!`\n" +
                                "- memberId: `11111111-1111-1111-1111-111111111101`\n" +
                                "- role: `USER`"))
                .servers(List.of(new Server().url("/").description("Gateway relative url")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("BearerAuth"));
    }
}
