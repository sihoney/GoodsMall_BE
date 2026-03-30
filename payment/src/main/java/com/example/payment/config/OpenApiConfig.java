package com.example.payment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * payment 모듈의 API 문서 메타데이터를 구성하고,
     * 게이트웨이가 주입하는 X-Member-Id / X-Member-Role 헤더를 Swagger Authorize에 등록한다.
     */
    @Bean
    public OpenAPI paymentOpenApi() {
        SecurityScheme memberIdScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Member-Id")
                .description("회원 UUID (예: 11111111-1111-1111-1111-111111111101)");

        SecurityScheme memberRoleScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Member-Role")
                .description("회원 역할 (USER 또는 ADMIN)");

        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("v1")
                        .description("payment service API documentation\n\n" +
                                "**인증 방법**: 우측 상단 Authorize 버튼 클릭 후\n" +
                                "- X-Member-Id: 테스트 구매자 → `11111111-1111-1111-1111-111111111101`\n" +
                                "- X-Member-Role: `USER`"))
                .components(new Components()
                        .addSecuritySchemes("X-Member-Id", memberIdScheme)
                        .addSecuritySchemes("X-Member-Role", memberRoleScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-Member-Id")
                        .addList("X-Member-Role"));
    }
}
