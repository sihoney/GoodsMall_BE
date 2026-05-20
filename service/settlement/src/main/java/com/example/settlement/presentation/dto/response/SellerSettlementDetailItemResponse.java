package com.example.settlement.presentation.dto.response;

import com.example.settlement.application.dto.SellerSettlementDetailItemResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerSettlementDetailItemResponse(
        UUID settlementItemId,
        UUID orderId,
        UUID escrowId,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        LocalDateTime releasedAt
) {

    public static SellerSettlementDetailItemResponse from(SellerSettlementDetailItemResult result) {
        return new SellerSettlementDetailItemResponse(
                result.settlementItemId(),
                result.orderId(),
                result.escrowId(),
                result.grossAmount(),
                result.feeAmount(),
                result.netAmount(),
                result.releasedAt()
        );
    }
}
