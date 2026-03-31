package com.example.order.application.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
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
