package com.example.payment.application.event;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPurchaseConfirmedEvent(
        UUID orderId,
        UUID sellerMemberId,
        LocalDateTime confirmedAt,
        ConfirmationType confirmationType
) {
}
