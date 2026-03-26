package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerIncomeReleasedMessage(
        UUID orderId,
        UUID sellerMemberId,
        UUID sellerWalletId,
        Long releasedAmount,
        LocalDateTime releasedAt,
        ConfirmationType confirmationType
) {
}
