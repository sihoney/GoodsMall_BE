package com.example.settlement.infrastructure.scheduler;

import com.example.settlement.application.service.MonthlySettlementService;
import com.example.settlement.application.service.SettlementPayoutService;
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

    private final MonthlySettlementService monthlySettlementService;
    private final SettlementPayoutService settlementPayoutService;

    public MonthlySettlementAggregationScheduler(
            MonthlySettlementService monthlySettlementService,
            SettlementPayoutService settlementPayoutService
    ) {
        this.monthlySettlementService = monthlySettlementService;
        this.settlementPayoutService = settlementPayoutService;
    }

    /**
     * KST 기준으로 직전월 집계를 실행하고 처리 결과를 로그로 남긴다.
     * <p>
     * 집계/일반 지급 요청 이후 RETRYABLE 실패 정산건 재지급 오케스트레이션을 같은 연월에 대해 이어서 수행한다.
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
        int retriedPayoutCount = settlementPayoutService.requestRetryableFailedPayouts(
                result.settlementYear(),
                result.settlementMonth()
        );
        log.info(
                "Monthly settlement aggregation finished. targetYear={}, targetMonth={}, created={}, updated={}, items={}, payoutRequested={}, payoutRetried={}",
                result.settlementYear(),
                result.settlementMonth(),
                result.createdSettlementCount(),
                result.updatedSettlementCount(),
                result.aggregatedItemCount(),
                requestedPayoutCount,
                retriedPayoutCount
        );
    }
}
