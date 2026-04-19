package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.PartialSettlementExecutionCommand;
import com.example.settlement.application.dto.PartialSettlementExecutionResult;

/**
 * 판매자 부분 정산 실행 유스케이스 진입점이다.
 */
public interface PartialSettlementExecutionUseCase {

    PartialSettlementExecutionResult executePartialSettlement(PartialSettlementExecutionCommand command);
}
