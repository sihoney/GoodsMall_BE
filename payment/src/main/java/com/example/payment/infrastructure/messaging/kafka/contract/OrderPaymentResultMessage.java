package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * payment가 order에 되돌려 주는 주문 결제 결과 Kafka 계약 메시지다.
 */
public record OrderPaymentResultMessage(
        String eventId,
        UUID orderId,
        UUID buyerMemberId,
        UUID sellerMemberId,
        OrderPaymentResultStatus status,
        Long paidAmount,
        Long sellerReceivableAmount,
        UUID buyerWalletId,
        UUID escrowId,
        OrderPaymentFailureReason failureReason,
        String failureMessage,
        LocalDateTime occurredAt
) {
}
