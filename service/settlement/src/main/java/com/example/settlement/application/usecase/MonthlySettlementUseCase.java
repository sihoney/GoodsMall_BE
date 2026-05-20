package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.domain.entity.SettlementItem;
import java.time.LocalDateTime;

/**
 * 정산 원천 적재 및 월 집계 유스케이스 진입점이다.
 */
public interface MonthlySettlementUseCase {

    SettlementItem registerSettlementItem(SettlementItemCreateCommand command);

    MonthlySettlementAggregateResult aggregatePreviousMonth(LocalDateTime referenceDateTime);
}
