package com.example.ai.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductCreatedMessage(
        String eventId,
        String productId,
        String productName,
        String title,
        String categoryName,
        String description,
        String status,
        String sourceUpdatedAt,
        String updatedAt,
        String occurredAt
) {
}

