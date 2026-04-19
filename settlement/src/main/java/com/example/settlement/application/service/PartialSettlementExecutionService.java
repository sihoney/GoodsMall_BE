package com.example.settlement.application.service;

import com.example.settlement.application.dto.PartialSettlementCreateCommand;
import com.example.settlement.application.dto.PartialSettlementCreateResult;
import com.example.settlement.application.dto.PartialSettlementExecutionCommand;
import com.example.settlement.application.dto.PartialSettlementExecutionResult;
import com.example.settlement.application.usecase.PartialSettlementCreationUseCase;
import com.example.settlement.application.usecase.PartialSettlementExecutionUseCase;
import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 부분 정산 생성과 지급 요청 발행을 함께 처리하는 서비스다.
 */
@Service
@Transactional
public class PartialSettlementExecutionService implements PartialSettlementExecutionUseCase {

    private final PartialSettlementCreationUseCase partialSettlementCreationUseCase;
    private final SettlementPayoutUseCase settlementPayoutUseCase;

    public PartialSettlementExecutionService(
            PartialSettlementCreationUseCase partialSettlementCreationUseCase,
            SettlementPayoutUseCase settlementPayoutUseCase
    ) {
        this.partialSettlementCreationUseCase = partialSettlementCreationUseCase;
        this.settlementPayoutUseCase = settlementPayoutUseCase;
    }

    @Override
    public PartialSettlementExecutionResult executePartialSettlement(PartialSettlementExecutionCommand command) {
        Objects.requireNonNull(command, "command must not be null.");

        PartialSettlementCreateResult partialSettlementCreateResult = partialSettlementCreationUseCase.createPartialSettlement(
                new PartialSettlementCreateCommand(
                        command.sellerId(),
                        command.settlementItemIds()
                )
        );
        settlementPayoutUseCase.requestPayoutForPartialSettlement(partialSettlementCreateResult.settlementId());

        return new PartialSettlementExecutionResult(
                partialSettlementCreateResult.settlementId(),
                partialSettlementCreateResult.sellerId(),
                partialSettlementCreateResult.settlementType(),
                partialSettlementCreateResult.settlementStatus(),
                partialSettlementCreateResult.settlementItemCount(),
                partialSettlementCreateResult.totalSalesAmount(),
                partialSettlementCreateResult.feeAmount(),
                partialSettlementCreateResult.finalSettlementAmount()
        );
    }
}
