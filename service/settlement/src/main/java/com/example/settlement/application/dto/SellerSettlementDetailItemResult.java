package com.example.settlement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SellerSettlementDetailItemResult(
        UUID settlementItemId,
        UUID orderId,
        UUID escrowId,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        LocalDateTime releasedAt
) {
}
