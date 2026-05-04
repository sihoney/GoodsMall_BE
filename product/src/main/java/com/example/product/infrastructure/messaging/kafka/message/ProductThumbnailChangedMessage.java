package com.example.product.infrastructure.messaging.kafka.message;

public record ProductThumbnailChangedMessage(
        String eventId,
        String productId,
        String thumbnailKey,
        String occurredAt
) {
}
