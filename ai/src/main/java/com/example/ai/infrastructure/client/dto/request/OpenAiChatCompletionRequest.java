package com.example.ai.infrastructure.client.dto.request;

import java.util.List;

public record OpenAiChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature
) {
    public record Message(
            String role,
            String content
    ) {
    }
}
