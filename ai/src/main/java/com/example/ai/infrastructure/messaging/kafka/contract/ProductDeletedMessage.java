package com.example.ai.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
// TODO: product에서 이벤트 발행에 따라 변경 가능
public record ProductDeletedMessage(
        String eventId,
        String productId,
        String sourceUpdatedAt,
        String updatedAt,
        String occurredAt
) {
}

