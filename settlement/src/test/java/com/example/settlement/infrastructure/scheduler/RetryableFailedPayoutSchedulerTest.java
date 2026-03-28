package com.example.settlement.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryableFailedPayoutScheduler 테스트")
class RetryableFailedPayoutSchedulerTest {

    @Mock
    private SettlementPayoutUseCase settlementPayoutService;

    @InjectMocks
    private RetryableFailedPayoutScheduler scheduler;

    @Test
    @DisplayName("재시도 배치는 현재 연월 기준으로 RETRYABLE 실패 재지급을 요청한다")
    void retryCurrentMonthFailedPayouts_requestsRetryableFailedPayouts() {
        when(settlementPayoutService.requestRetryableFailedPayouts(anyInt(), anyInt())).thenReturn(1);

        scheduler.retryCurrentMonthFailedPayouts();

        verify(settlementPayoutService).requestRetryableFailedPayouts(anyInt(), anyInt());
    }
}

