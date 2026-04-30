package com.example.product.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCanceledPayload(
        UUID orderId,
        UUID buyerId,
        Instant canceledAt,
        List<CanceledOrderLine> canceledLines
) {
    public record CanceledOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {}
}
