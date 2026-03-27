package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.WalletSummaryResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletSummaryResponse(
        UUID walletId,
        UUID memberId,
        Long balance,
        LocalDateTime updatedAt
) {

    public static WalletSummaryResponse from(WalletSummaryResult result) {
        return new WalletSummaryResponse(
                result.walletId(),
                result.memberId(),
                result.balance(),
                result.updatedAt()
        );
    }
}
