package com.example.ai.infrastructure.client.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenAiMultimodalChatCompletionRequest(
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
            List<ContentPart> content
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ContentPart(
            String type,
            String text,
            @JsonProperty("image_url")
            ImageUrl imageUrl
    ) {
        public static ContentPart text(String text) {
            return new ContentPart("text", text, null);
        }

        public static ContentPart imageUrl(String url) {
            return new ContentPart("image_url", null, new ImageUrl(url));
        }
    }

    public record ImageUrl(
            String url
    ) {
    }
}
