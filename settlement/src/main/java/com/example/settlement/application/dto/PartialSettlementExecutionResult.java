package com.example.settlement.application.dto;

import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.UUID;

/**
 * 판매자 부분 정산 실행 결과다.
 */
public record PartialSettlementExecutionResult(
        UUID settlementId,
        UUID sellerId,
        SettlementType settlementType,
        SettlementStatus settlementStatus,
        int settlementItemCount,
        Long totalSalesAmount,
        Long feeAmount,
        Long finalSettlementAmount
) {
}
