package com.example.order.infrastructure.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentResultEvent(
        UUID eventId,
        UUID orderId,
        String status,
        String failReason,
        Instant occurredAt
) {
}
