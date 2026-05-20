package com.example.product.infrastructure.messaging.kafka.message;

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
