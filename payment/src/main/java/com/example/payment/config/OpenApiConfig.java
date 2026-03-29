package com.example.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * payment 모듈의 API 문서 메타데이터(제목, 버전, 설명)를 구성한다.
     */
    @Bean
    public OpenAPI paymentOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Payment Service API")
                .version("v1")
                .description("payment service API documentation"));
    }
}

