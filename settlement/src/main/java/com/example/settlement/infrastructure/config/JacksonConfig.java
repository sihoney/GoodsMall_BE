package com.example.settlement.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper 설정.
 * Spring Boot 4.0에서 ObjectMapper가 자동 등록되지 않으므로 명시적으로 빈을 등록한다.
 * - JavaTimeModule: LocalDateTime 등 Java 8 날짜/시간 타입 직렬화 지원
 * - WRITE_DATES_AS_TIMESTAMPS 비활성화: 날짜를 ISO-8601 문자열로 직렬화
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}

