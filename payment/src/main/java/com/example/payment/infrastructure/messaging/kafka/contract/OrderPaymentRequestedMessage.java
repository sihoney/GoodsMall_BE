package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * order 서비스가 payment에 전달하는 주문 결제 요청 Kafka 계약 메시지다.
 */
public record OrderPaymentRequestedMessage(
        String eventId,
        UUID orderId,
        UUID buyerMemberId,
        UUID sellerMemberId,
        Long orderAmount,
        Long sellerReceivableAmount,
        LocalDateTime occurredAt
) {
}
