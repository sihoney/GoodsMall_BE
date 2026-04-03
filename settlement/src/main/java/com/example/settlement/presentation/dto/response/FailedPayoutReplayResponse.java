package com.example.settlement.presentation.dto.response;

import com.example.settlement.application.dto.FailedPayoutReplayResult;

/**
 * DLQ replay 실행 결과 응답 DTO다.
 */
public record FailedPayoutReplayResponse(
        int requestedRetryCount,
        int manualActionRequiredCount,
        int skippedCount,
        int notFoundCount
) {

    public static FailedPayoutReplayResponse from(FailedPayoutReplayResult result) {
        return new FailedPayoutReplayResponse(
                result.requestedRetryCount(),
                result.manualActionRequiredCount(),
                result.skippedCount(),
                result.notFoundCount()
        );
    }
}

