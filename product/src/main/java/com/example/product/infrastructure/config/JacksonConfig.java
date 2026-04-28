package com.example.product.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Kafka outbox payload / 일반 Web JSON 직렬화용 Jackson 2 ObjectMapper.
     * Elasticsearch 통신은 ElasticsearchConfig#jsonpMapper() 의 Jackson 3 매퍼를 사용한다.
     * ES 응답 변환에 이 빈을 끼워넣지 말 것.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
