package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.PartialSettlementCreateCommand;
import com.example.settlement.application.dto.PartialSettlementCreateResult;

/**
 * 판매자 부분 정산 생성 유스케이스 진입점이다.
 */
public interface PartialSettlementCreationUseCase {

    PartialSettlementCreateResult createPartialSettlement(PartialSettlementCreateCommand command);
}
