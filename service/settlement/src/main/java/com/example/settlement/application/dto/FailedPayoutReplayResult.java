package com.example.settlement.application.dto;

/**
 * DLQ replay(리플레이) 대상 정산건 처리 결과 요약 DTO다.
 */
public record FailedPayoutReplayResult(
        int requestedRetryCount,
        int manualActionRequiredCount,
        int skippedCount,
        int notFoundCount
) {
}

