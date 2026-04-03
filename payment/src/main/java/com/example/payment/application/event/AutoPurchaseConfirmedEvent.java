package com.example.payment.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AutoPurchaseConfirmedEvent(
        UUID orderId,
        UUID buyerMemberId,
        LocalDateTime confirmedAt
) {
}
