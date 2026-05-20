package com.example.settlement.application.dto;

import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerSettlementListItemResult(
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
        LocalDateTime updatedAt
) {
}
