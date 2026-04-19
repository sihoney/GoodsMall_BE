package com.example.settlement.presentation.dto.response;

import com.example.settlement.application.dto.PartialSettlementAvailableItemResult;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 부분 정산 가능 항목 응답이다.
 */
public record PartialSettlementAvailableItemResponse(
        UUID settlementItemId,
        UUID escrowId,
        UUID orderId,
        Long grossAmount,
        Long feeAmount,
        Long netAmount,
        LocalDateTime releasedAt
) {

    public static PartialSettlementAvailableItemResponse from(PartialSettlementAvailableItemResult result) {
        return new PartialSettlementAvailableItemResponse(
                result.settlementItemId(),
                result.escrowId(),
                result.orderId(),
                result.grossAmount(),
                result.feeAmount(),
                result.netAmount(),
                result.releasedAt()
        );
    }
}
