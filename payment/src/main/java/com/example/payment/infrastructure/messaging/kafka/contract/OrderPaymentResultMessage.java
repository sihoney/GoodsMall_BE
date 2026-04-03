package com.example.payment.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * payment가 order에 되돌려 주는 주문 결제 결과 Kafka 계약 메시지다.
 */
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
