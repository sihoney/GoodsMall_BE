package com.example.notification.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCanceledMessage(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID buyerId,
        Instant canceledAt,
        Instant eventCreatedAt,
        List<CanceledOrderLine> canceledLines
) {

    public record CanceledOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }
}
