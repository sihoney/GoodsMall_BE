package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.FailedPayoutReplayResult;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import java.util.List;
import java.util.UUID;

/**
 * 정산 지급/재시도/수동조치 오케스트레이션 유스케이스 진입점이다.
 */
public interface SettlementPayoutUseCase {

    int requestMonthlyPayouts(int settlementYear, int settlementMonth);

    void applyPayoutResult(SellerSettlementPayoutResultMessage event);

    int requestRetryableFailedPayouts(int settlementYear, int settlementMonth);

    boolean requestManualFailedPayout(UUID settlementId);

    FailedPayoutReplayResult replayFailedPayouts(List<UUID> settlementIds);
}
