package com.example.ai.infrastructure.client.dto.response;

import java.util.List;

public record OpenAiChatCompletionResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String content
    ) {
    }
}
