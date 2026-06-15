package com.example.notification.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

// Kafka 메시지로 사용할 record 클래스
public record AutoPurchaseConfirmedMessage(
        UUID orderId,
        UUID buyerMemberId,
        Instant confirmedAt
) {
}
