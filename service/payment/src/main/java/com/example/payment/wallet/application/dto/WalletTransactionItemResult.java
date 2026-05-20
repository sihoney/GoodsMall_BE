package com.example.payment.wallet.application.dto;

import com.example.payment.wallet.domain.enumtype.WalletTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 嫄곕옒 ?댁뿭 紐⑸줉???④굔 寃곌낵??
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
