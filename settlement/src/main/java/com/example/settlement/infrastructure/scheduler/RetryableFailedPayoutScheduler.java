package com.example.settlement.infrastructure.scheduler;

import com.example.settlement.application.service.SettlementPayoutService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RETRYABLE 실패 정산건 재지급 오케스트레이션을 주기적으로 실행하는 스케줄러다.
 */
@Component
public class RetryableFailedPayoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryableFailedPayoutScheduler.class);
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final SettlementPayoutService settlementPayoutService;

    public RetryableFailedPayoutScheduler(SettlementPayoutService settlementPayoutService) {
        this.settlementPayoutService = settlementPayoutService;
    }

    /**
     * KST 현재 연월 기준으로 RETRYABLE 실패 정산건 재지급 요청을 실행한다.
     */
    @Scheduled(
            cron = "${settlement.batch.retryable-failed-payout.cron:0 */10 * * * *}",
            zone = "${settlement.batch.retryable-failed-payout.zone:Asia/Seoul}"
    )
    public void retryCurrentMonthFailedPayouts() {
        LocalDateTime nowInKorea = LocalDateTime.now(KOREA_ZONE_ID);
        int retriedCount = settlementPayoutService.requestRetryableFailedPayouts(
                nowInKorea.getYear(),
                nowInKorea.getMonthValue()
        );

        log.info(
                "Retryable failed payout scheduler finished. targetYear={}, targetMonth={}, payoutRetried={}",
                nowInKorea.getYear(),
                nowInKorea.getMonthValue(),
                retriedCount
        );
    }
}

