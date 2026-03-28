package com.example.settlement.infrastructure.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.service.MonthlySettlementService;
import com.example.settlement.application.service.SettlementPayoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlySettlementAggregationScheduler 테스트")
class MonthlySettlementAggregationSchedulerTest {

    @Mock
    private MonthlySettlementService monthlySettlementService;

    @Mock
    private SettlementPayoutService settlementPayoutService;

    @InjectMocks
    private MonthlySettlementAggregationScheduler scheduler;

    @Test
    @DisplayName("월 집계 배치는 집계 후 일반 지급 요청만 수행한다")
    void aggregatePreviousMonth_requestsMonthlyPayoutOnly() {
        when(monthlySettlementService.aggregatePreviousMonth(any())).thenReturn(new MonthlySettlementAggregateResult(
                2026,
                3,
                1,
                0,
                10
        ));
        when(settlementPayoutService.requestMonthlyPayouts(2026, 3)).thenReturn(1);

        scheduler.aggregatePreviousMonth();

        verify(monthlySettlementService).aggregatePreviousMonth(any());
        verify(settlementPayoutService).requestMonthlyPayouts(2026, 3);
        verify(settlementPayoutService, never()).requestRetryableFailedPayouts(2026, 3);
    }
}

