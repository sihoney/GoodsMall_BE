package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.WithdrawStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawListItemResult(
        UUID withdrawRequestId,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal actualAmount,
        String maskedBankAccount,
        WithdrawStatus status,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {
}
