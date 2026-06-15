package com.example.payment.common.config;

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
     * payment 紐⑤뱢??API 臾몄꽌 硫뷀??곗씠?곕? 援ъ꽦?섍퀬,
     * Gateway Swagger UI?먯꽌 Bearer ?좏겙???낅젰諛쏆븘 ?몄텧?????덈룄濡?臾몄꽌 蹂댁븞 ?ㅽ궎留덈? 援ъ꽦?쒕떎.
     * servers瑜?"/"(?곷?寃쎈줈)濡?怨좎젙?섏뿬 Swagger UI媛 ?대┛ origin(gateway) 湲곗??쇰줈 API瑜??몄텧?섎룄濡??쒕떎.
     */
    @Bean
    public OpenAPI paymentOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("member-service 濡쒓렇?몄쑝濡?諛쏆? accessToken???낅젰?섎㈃ gateway媛 X-Member-Id / X-Member-Role ?ㅻ뜑瑜??대??곸쑝濡?二쇱엯?⑸땲??");

        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("v1")
                        .description("payment service API documentation\n\n" +
                                "**沅뚯옣 ?뚯뒪???쒖꽌**\n" +
                                "1. member-service `POST /api/auth/login` ?몄텧\n" +
                                "2. ?묐떟??`accessToken` 蹂듭궗\n" +
                                "3. payment-service ?곗륫 ?곷떒 Authorize??`Bearer <accessToken>` ?낅젰\n\n" +
                                "**buyer seed 怨꾩젙**\n" +
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
