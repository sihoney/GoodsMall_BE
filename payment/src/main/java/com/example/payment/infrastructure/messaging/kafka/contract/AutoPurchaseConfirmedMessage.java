package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * 자동 구매확정 완료를 외부 모듈에 알리는 Kafka 계약 메시지다.
 */
public record AutoPurchaseConfirmedMessage(
        UUID orderId,
        UUID buyerMemberId,
        Instant confirmedAt
) {
}
