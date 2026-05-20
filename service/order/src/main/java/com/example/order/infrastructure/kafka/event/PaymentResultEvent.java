package com.example.order.infrastructure.kafka.event;

import java.util.UUID;

public record PaymentResultEvent(
        UUID orderId,
        String status,
        String failReason
) {
}
