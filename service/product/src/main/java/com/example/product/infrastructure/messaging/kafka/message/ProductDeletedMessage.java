package com.example.product.infrastructure.messaging.kafka.message;

public record ProductDeletedMessage(
        String eventId,
        String productId,
        String sourceUpdatedAt,
        String updatedAt,
        String occurredAt
) {
}
