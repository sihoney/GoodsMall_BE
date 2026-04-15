package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 미정산 escrow 목록 조회 결과다.
 */
public record PendingSellerIncomeItemResult(
        UUID escrowId,
        UUID orderId,
        Long amount,
        EscrowStatus escrowStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
