package com.example.ai.infrastructure.client.dto.request;

public record OpenAiEmbeddingRequest(
        String model,
        String input
) {
}
