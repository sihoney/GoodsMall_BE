package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.WalletTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 거래 내역 목록의 단건 결과다.
 */
public record WalletTransactionItemResult(
        UUID transactionId,
        WalletTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceType,
        UUID referenceId,
        String description,
        LocalDateTime createdAt
) {
}
