package com.example.payment.escrow.application.dto;

import com.example.payment.escrow.domain.enumtype.EscrowTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowTransactionItemResult(
        UUID escrowTransactionId,
        UUID escrowId,
        UUID orderId,
        UUID orderItemId,
        UUID sellerMemberId,
        UUID buyerMemberId,
        EscrowTransactionType transactionType,
        BigDecimal amount,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        UUID referenceId,
        String referenceType,
        String description,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
}
