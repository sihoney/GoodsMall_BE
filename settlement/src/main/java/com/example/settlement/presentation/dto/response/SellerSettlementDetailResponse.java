package com.example.settlement.presentation.dto.response;

import com.example.settlement.application.dto.SellerSettlementDetailResult;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SellerSettlementDetailResponse(
        UUID settlementId,
        UUID sellerId,
        SettlementType settlementType,
        Integer settlementYear,
        Integer settlementMonth,
        BigDecimal totalSalesAmount,
        BigDecimal feeAmount,
        BigDecimal finalSettlementAmount,
        BigDecimal settledAmount,
        SettlementStatus settlementStatus,
        LocalDateTime settledAt,
        String lastFailureReason,
        LocalDateTime requestedAt,
        LocalDateTime updatedAt,
        List<SellerSettlementDetailItemResponse> items
) {

    public static SellerSettlementDetailResponse from(SellerSettlementDetailResult result) {
        return new SellerSettlementDetailResponse(
                result.settlementId(),
                result.sellerId(),
                result.settlementType(),
                result.settlementYear(),
                result.settlementMonth(),
                result.totalSalesAmount(),
                result.feeAmount(),
                result.finalSettlementAmount(),
                result.settledAmount(),
                result.settlementStatus(),
                result.settledAt(),
                result.lastFailureReason(),
                result.requestedAt(),
                result.updatedAt(),
                result.items().stream().map(SellerSettlementDetailItemResponse::from).toList()
        );
    }
}
