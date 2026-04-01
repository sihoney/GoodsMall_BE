package com.example.order.infrastructure.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResultEvent(
        UUID eventId,
        UUID orderId,
        BigDecimal amount,
        Instant occurredAt,
        String status,
        String reasonCode
) {
}
