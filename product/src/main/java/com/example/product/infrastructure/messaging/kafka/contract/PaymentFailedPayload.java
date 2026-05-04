package com.example.product.infrastructure.messaging.kafka.contract;

import java.util.List;
import java.util.UUID;

public record PaymentFailedPayload(
        UUID orderId,
        UUID buyerId,
        List<FailedOrderLine> failedLines
) {
    public record FailedOrderLine(
            UUID productId,
            UUID sellerId,
            int quantity
    ) {}
}
