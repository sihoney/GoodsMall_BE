package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.EscrowTransactionItemResult;
import com.example.payment.domain.enumtype.EscrowTransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowTransactionItemResponse(
        UUID escrowTransactionId,
        UUID escrowId,
        UUID orderId,
        UUID orderItemId,
        UUID sellerMemberId,
        UUID buyerMemberId,
        EscrowTransactionType transactionType,
        Long amount,
        Long beforeAmount,
        Long afterAmount,
        UUID referenceId,
        String referenceType,
        String description,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
    public static EscrowTransactionItemResponse from(EscrowTransactionItemResult result) {
        return new EscrowTransactionItemResponse(
                result.escrowTransactionId(),
                result.escrowId(),
                result.orderId(),
                result.orderItemId(),
                result.sellerMemberId(),
                result.buyerMemberId(),
                result.transactionType(),
                result.amount(),
                result.beforeAmount(),
                result.afterAmount(),
                result.referenceId(),
                result.referenceType(),
                result.description(),
                result.occurredAt(),
                result.createdAt()
        );
    }
}

