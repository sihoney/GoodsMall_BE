package com.example.settlement.application.dto;

/**
 * 월 단위 정산 집계 결과를 담는 DTO다.
 * 신규 생성/업데이트/집계된 주문 아이템 건수 등을 추적한다.
 */
public record MonthlySettlementAggregateResult(
        int settlementYear,
        int settlementMonth,
        int createdSettlementCount,
        int updatedSettlementCount,
        int aggregatedItemCount
) {
}
