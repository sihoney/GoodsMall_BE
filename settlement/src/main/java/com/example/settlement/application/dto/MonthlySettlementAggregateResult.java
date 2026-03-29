package com.example.settlement.application.dto;

public record MonthlySettlementAggregateResult(
        int settlementYear,
        int settlementMonth,
        int createdSettlementCount,
        int updatedSettlementCount,
        int aggregatedItemCount
) {
}
