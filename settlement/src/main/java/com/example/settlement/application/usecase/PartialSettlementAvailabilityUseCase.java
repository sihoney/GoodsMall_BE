package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.PartialSettlementAvailableItemResult;
import java.util.List;
import java.util.UUID;

/**
 * 판매자 부분 정산 가능 항목 조회 유스케이스 진입점이다.
 */
public interface PartialSettlementAvailabilityUseCase {

    List<PartialSettlementAvailableItemResult> findAvailableItemsForPartialSettlement(UUID sellerId);
}
