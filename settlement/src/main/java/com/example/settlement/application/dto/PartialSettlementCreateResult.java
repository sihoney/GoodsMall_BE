package com.example.settlement.application.dto;

import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.UUID;

/**
 * 판매자 부분 정산 생성 결과다.
 */
public record PartialSettlementCreateResult(
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
