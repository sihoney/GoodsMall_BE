package com.todaylunch.auction.infrastructure.messaging.kafka.message;

public record ProductThumbnailChangedPayload(
        String eventId,
        String productId,
        String thumbnailKey,
        String occurredAt
) {
}
