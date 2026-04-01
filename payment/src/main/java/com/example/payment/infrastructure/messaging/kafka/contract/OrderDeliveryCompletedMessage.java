package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * order 배송완료 이벤트의 Kafka 계약 메시지다.
 */
public record OrderDeliveryCompletedMessage(
        String eventId,
        UUID orderId,
        Instant deliveredAt,
        Instant occurredAt
) {
}
