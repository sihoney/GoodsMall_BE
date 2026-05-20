package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.domain.enumtype.WalletTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * wallet 거래 단건 응답 DTO다.
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
     * application 결과를 presentation 응답으로 변환한다.
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
