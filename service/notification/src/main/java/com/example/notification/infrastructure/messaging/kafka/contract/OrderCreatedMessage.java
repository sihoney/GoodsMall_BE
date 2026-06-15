package com.example.notification.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedMessage(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant orderCreatedAt,
        Instant eventCreatedAt,
        List<OrderLine> orderLines
) {

    public record OrderLine(
            UUID orderItemId,
            UUID sellerId,
            BigDecimal unitPriceSnapshot,
            int quantity,
            BigDecimal lineTotalPrice
    ) {
    }
}
