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
     * 월 집계가 끝난 직후 같은 대상 연월의 PENDING 정산건에 대해 지급 요청까지 이어서 실행한다.
     * 이렇게 하면 배치 운영 기준에서 "집계 완료 -> 지급 요청 준비" 흐름을 한 번에 추적할 수 있다.
     * <p>
     * RETRYABLE 실패 재시도 배치는 별도 스케줄러가 담당한다.
     */
    @Scheduled(
            // todo: 스케줄링 시간에 대해서 운영팀과 협의 필요. 현재는 매월 1일 새벽 3시 5분으로 설정했지만, 집계 소요 시간에 따라 조정 가능성 있음.
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
        //todo : @Slf4j로 변경 고려
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
