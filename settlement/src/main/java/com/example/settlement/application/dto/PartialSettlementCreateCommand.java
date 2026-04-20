package com.example.settlement.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * 판매자 부분 정산 생성 요청 데이터다.
 */
public record PartialSettlementCreateCommand(
        UUID sellerId,
        List<UUID> settlementItemIds
) {
}
