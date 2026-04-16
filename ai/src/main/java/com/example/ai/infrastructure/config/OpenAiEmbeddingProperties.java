package com.example.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.embedding")
public record OpenAiEmbeddingProperties(
        String model,
        String openaiApiKey,
        String openaiBaseUrl
) {
}
