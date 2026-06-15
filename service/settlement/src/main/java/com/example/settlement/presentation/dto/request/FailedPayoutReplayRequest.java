package com.example.settlement.presentation.dto.request;

import java.util.List;

/**
 * DLQ replay 대상 settlementId 목록 입력 DTO다.
 */
public record FailedPayoutReplayRequest(
        List<String> settlementIds
) {
}

