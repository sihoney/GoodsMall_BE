package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManualPurchaseConfirmResponse(
        UUID orderId,
        UUID sellerWalletId,
        Long releasedAmount,
        Long sellerWalletBalance,
        EscrowStatus escrowStatus,
        LocalDateTime releasedAt
) {

    public static ManualPurchaseConfirmResponse from(EscrowReleaseResult result) {
        return new ManualPurchaseConfirmResponse(
                result.orderId(),
                result.sellerWalletId(),
                result.releasedAmount(),
                result.sellerWalletBalance(),
                result.escrowStatus(),
                result.releasedAt()
        );
    }
}
