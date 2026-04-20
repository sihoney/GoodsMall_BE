package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.WithdrawResult;
import com.example.payment.domain.enumtype.WithdrawStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawResponse(
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

    public static WithdrawResponse from(WithdrawResult result) {
        return new WithdrawResponse(
                result.withdrawRequestId(),
                result.amount(),
                result.fee(),
                result.actualAmount(),
                result.maskedBankAccount(),
                result.status(),
                result.walletBalance(),
                result.requestedAt(),
                result.processedAt()
        );
    }
}
