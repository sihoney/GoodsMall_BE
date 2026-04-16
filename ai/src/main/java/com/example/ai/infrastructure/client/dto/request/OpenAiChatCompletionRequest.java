package com.example.ai.infrastructure.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenAiChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        @JsonProperty("response_format")
        ResponseFormat responseFormat
) {
    public record ResponseFormat(
            String type
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
