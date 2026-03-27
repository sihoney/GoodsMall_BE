package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 미정산 escrow 단건 응답 DTO다.
 */
public record PendingSellerIncomeItemResponse(
        UUID escrowId,
        UUID orderId,
        Long amount,
        EscrowStatus escrowStatus,
        LocalDateTime releaseAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * application 결과를 presentation 응답으로 변환한다.
     */
    public static PendingSellerIncomeItemResponse from(PendingSellerIncomeItemResult result) {
        return new PendingSellerIncomeItemResponse(
                result.escrowId(),
                result.orderId(),
                result.amount(),
                result.escrowStatus(),
                result.releaseAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
