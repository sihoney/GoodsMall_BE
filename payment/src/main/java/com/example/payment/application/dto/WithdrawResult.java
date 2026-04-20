package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.WithdrawStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawResult(
        UUID withdrawRequestId,
        Long amount,
        Long fee,
        Long actualAmount,
        String maskedBankAccount,
        WithdrawStatus status,
        Long walletBalance,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {
}
