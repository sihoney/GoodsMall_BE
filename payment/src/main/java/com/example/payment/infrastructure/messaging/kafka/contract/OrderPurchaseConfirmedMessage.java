package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPurchaseConfirmedMessage(
        String eventId,
        UUID orderId,
        UUID sellerMemberId,
        LocalDateTime confirmedAt,
        ConfirmationType confirmationType
) {
}
