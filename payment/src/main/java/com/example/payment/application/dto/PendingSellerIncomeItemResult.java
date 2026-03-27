package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingSellerIncomeItemResult(
        UUID escrowId,
        UUID orderId,
        Long amount,
        EscrowStatus escrowStatus,
        LocalDateTime releaseAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
