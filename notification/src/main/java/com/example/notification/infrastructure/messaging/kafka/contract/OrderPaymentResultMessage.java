package com.example.notification.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPaymentResultMessage(
        UUID eventId,
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal amount,
        OrderPaymentResultStatus status,
        OrderPaymentFailureReason reasonCode,
        Instant occurredAt
) {
}
