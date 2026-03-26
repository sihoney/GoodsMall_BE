package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseResult(
        UUID orderId,
        UUID sellerWalletId,
        Long releasedAmount,
        Long sellerWalletBalance,
        EscrowStatus escrowStatus,
        LocalDateTime releasedAt
) {
}
