package com.example.settlement.infrastructure.scheduler;

import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매월 직전월 정산 집계를 트리거하는 스케줄러다.
 */
@Component
public class MonthlySettlementAggregationScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonthlySettlementAggregationScheduler.class);
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MonthlySettlementUseCase monthlySettlementService;
    private final SettlementPayoutUseCase settlementPayoutService;

    public MonthlySettlementAggregationScheduler(
            MonthlySettlementUseCase monthlySettlementService,
            SettlementPayoutUseCase settlementPayoutService
    ) {
        this.monthlySettlementService = monthlySettlementService;
        this.settlementPayoutService = settlementPayoutService;
    }

    /**
     * KST 기준으로 직전월 집계를 실행하고 일반 지급 요청까지 처리 결과를 로그로 남긴다.
     * <p>
     * RETRYABLE 실패 재시도 배치는 별도 스케줄러가 담당한다.
     */
    @Scheduled(
            cron = "${settlement.batch.monthly-aggregation.cron:0 5 3 1 * *}",
            zone = "${settlement.batch.monthly-aggregation.zone:Asia/Seoul}"
    )
    public void aggregatePreviousMonth() {
        LocalDateTime nowInKorea = LocalDateTime.now(KOREA_ZONE_ID);
        var result = monthlySettlementService.aggregatePreviousMonth(nowInKorea);
        int requestedPayoutCount = settlementPayoutService.requestMonthlyPayouts(
                result.settlementYear(),
                result.settlementMonth()
        );
        log.info(
                "Monthly settlement aggregation finished. targetYear={}, targetMonth={}, created={}, updated={}, items={}, payoutRequested={}",
                result.settlementYear(),
                result.settlementMonth(),
                result.createdSettlementCount(),
                result.updatedSettlementCount(),
                result.aggregatedItemCount(),
                requestedPayoutCount
        );
    }
}
