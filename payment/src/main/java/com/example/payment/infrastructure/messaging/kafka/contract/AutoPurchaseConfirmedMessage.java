package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record AutoPurchaseConfirmedMessage(
        UUID orderId,
        UUID buyerMemberId,
        LocalDateTime confirmedAt
) {
}
