package com.example.notification.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

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
        Instant occurredAt
) {
}
