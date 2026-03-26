package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDeliveryCompletedMessage(
        String eventId,
        UUID orderId,
        LocalDateTime deliveredAt,
        LocalDateTime occurredAt
) {
}
