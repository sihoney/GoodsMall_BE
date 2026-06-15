package com.example.payment.wallet.presentation.dto.response;

import com.example.payment.wallet.application.dto.WalletTransactionItemResult;
import com.example.payment.wallet.domain.enumtype.WalletTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 嫄곕옒 ?④굔 ?묐떟 DTO??
 */
public record WalletTransactionItemResponse(
        UUID transactionId,
        WalletTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceType,
        UUID referenceId,
        String description,
        LocalDateTime createdAt
) {

    /**
     * application 寃곌낵瑜?presentation ?묐떟?쇰줈 蹂?섑븳??
     */
    public static WalletTransactionItemResponse from(WalletTransactionItemResult result) {
        return new WalletTransactionItemResponse(
                result.transactionId(),
                result.transactionType(),
                result.amount(),
                result.balanceAfter(),
                result.referenceType(),
                result.referenceId(),
                result.description(),
                result.createdAt()
        );
    }
}
