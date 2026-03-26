package com.example.payment.application.event;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerIncomeReleasedEvent(
        UUID orderId,
        UUID sellerMemberId,
        UUID sellerWalletId,
        Long releasedAmount,
        LocalDateTime releasedAt,
        ConfirmationType confirmationType
) {
}
