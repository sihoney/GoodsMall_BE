package com.example.settlement.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * 판매자 부분 정산 실행 요청 데이터다.
 */
public record PartialSettlementExecutionCommand(
        UUID sellerId,
        List<UUID> settlementItemIds
) {
}
