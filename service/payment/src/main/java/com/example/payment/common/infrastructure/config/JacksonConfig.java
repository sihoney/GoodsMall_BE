package com.example.payment.common.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson ObjectMapper ?ㅼ젙.
 * Spring Boot 4.0?먯꽌 ObjectMapper媛 ?먮룞 ?깅줉?섏? ?딆쑝誘濡?紐낆떆?곸쑝濡?鍮덉쓣 ?깅줉?쒕떎.
 * - JavaTimeModule: LocalDateTime ??Java 8 ?좎쭨/?쒓컙 ???吏곷젹??吏?? * - WRITE_DATES_AS_TIMESTAMPS 鍮꾪솢?깊솕: ?좎쭨瑜?ISO-8601 臾몄옄?대줈 吏곷젹?? */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().build();
    }
}

