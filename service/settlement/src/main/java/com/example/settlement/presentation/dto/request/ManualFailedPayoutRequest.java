package com.example.settlement.presentation.dto.request;

/**
 * FAILED 정산건 수동 재지급 요청 입력 DTO다.
 */
public record ManualFailedPayoutRequest(
        String settlementId
) {
}

