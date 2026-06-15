package com.example.member.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Keep member module serialization aligned with Spring Boot 4 / Jackson 3.
        return JsonMapper.builder().build();
    }
}
