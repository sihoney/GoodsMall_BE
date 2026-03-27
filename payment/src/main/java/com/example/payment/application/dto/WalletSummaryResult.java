package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletSummaryResult(
        UUID walletId,
        UUID memberId,
        Long balance,
        LocalDateTime updatedAt
) {
}
