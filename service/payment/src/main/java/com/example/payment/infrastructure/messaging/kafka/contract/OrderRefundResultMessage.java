package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderRefundResultMessage(
        UUID eventId,
        UUID refundId,
        UUID orderId,
        List<UUID> orderItemIds,
        OrderRefundResultStatus status,
        String reasonCode,
        Instant occurredAt
) {
}
