package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.WithdrawListItemResult;
import com.example.payment.wallet.domain.enumtype.WithdrawStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawListItemResponse(
        UUID withdrawRequestId,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal actualAmount,
        String maskedBankAccount,
        WithdrawStatus status,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {

    public static WithdrawListItemResponse from(WithdrawListItemResult result) {
        return new WithdrawListItemResponse(
                result.withdrawRequestId(),
                result.amount(),
                result.fee(),
                result.actualAmount(),
                result.maskedBankAccount(),
                result.status(),
                result.requestedAt(),
                result.processedAt()
        );
    }
}
