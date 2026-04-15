package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowTransactionType;
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
        Long amount,
        Long beforeAmount,
        Long afterAmount,
        UUID referenceId,
        String referenceType,
        String description,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
}

