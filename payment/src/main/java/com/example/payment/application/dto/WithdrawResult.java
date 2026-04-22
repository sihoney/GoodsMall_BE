package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.WithdrawStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawResult(
        UUID withdrawRequestId,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal actualAmount,
        String maskedBankAccount,
        WithdrawStatus status,
        BigDecimal walletBalance,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {
}
