package com.example.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.recommendation.rerank")
public record RecommendationRerankProperties(
        boolean enabled,
        String model,
        String openaiApiKey,
        String openaiBaseUrl,
        double temperature,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
