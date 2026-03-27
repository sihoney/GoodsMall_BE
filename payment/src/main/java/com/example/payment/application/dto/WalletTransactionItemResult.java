package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.WalletTransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletTransactionItemResult(
        UUID transactionId,
        WalletTransactionType transactionType,
        Long amount,
        Long balanceAfter,
        String referenceType,
        UUID referenceId,
        String description,
        LocalDateTime createdAt
) {
}
