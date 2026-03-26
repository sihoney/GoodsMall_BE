package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

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
