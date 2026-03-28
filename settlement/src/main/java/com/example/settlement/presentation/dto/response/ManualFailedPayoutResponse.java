package com.example.settlement.presentation.dto.response;

import java.util.UUID;

/**
 * FAILED 정산건 수동 재지급 요청 결과 응답 DTO다.
 */
public record ManualFailedPayoutResponse(
        UUID settlementId,
        boolean requested,
        String message
) {

    public static ManualFailedPayoutResponse requested(UUID settlementId) {
        return new ManualFailedPayoutResponse(settlementId, true, "Manual payout retry requested.");
    }
}

