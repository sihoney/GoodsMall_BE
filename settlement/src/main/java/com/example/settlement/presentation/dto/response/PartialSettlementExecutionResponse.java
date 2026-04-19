package com.example.settlement.presentation.dto.response;

import com.example.settlement.application.dto.PartialSettlementExecutionResult;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.UUID;

/**
 * 판매자 부분 정산 실행 응답이다.
 */
public record PartialSettlementExecutionResponse(
        UUID settlementId,
        UUID sellerId,
        SettlementType settlementType,
        SettlementStatus settlementStatus,
        int settlementItemCount,
        Long totalSalesAmount,
        Long feeAmount,
        Long finalSettlementAmount
) {

    public static PartialSettlementExecutionResponse from(PartialSettlementExecutionResult result) {
        return new PartialSettlementExecutionResponse(
                result.settlementId(),
                result.sellerId(),
                result.settlementType(),
                result.settlementStatus(),
                result.settlementItemCount(),
                result.totalSalesAmount(),
                result.feeAmount(),
                result.finalSettlementAmount()
        );
    }
}
