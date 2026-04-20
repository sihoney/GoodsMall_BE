package com.example.settlement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자가 부분 정산 화면에서 선택할 수 있는 정산 대기 항목 조회 결과다.
 */
public record PartialSettlementAvailableItemResult(
        UUID settlementItemId,
        UUID escrowId,
        UUID orderId,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        LocalDateTime releasedAt
) {
}
