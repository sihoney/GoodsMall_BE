package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeDetailResponse(
        UUID chargeId,
        UUID memberId,
        UUID walletId,
        Long requestedAmount,
        Long approvedAmount,
        PgProvider pgProvider,
        String pgOrderId,
        String pgPaymentKey,
        ChargeStatus chargeStatus,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime failedAt,
        String failureReason,
        boolean hasRefundHistory,
        ChargeRefundSummaryResponse latestRefund
) {

    public static ChargeDetailResponse from(ChargeDetailResult result) {
        return new ChargeDetailResponse(
                result.chargeId(),
                result.memberId(),
                result.walletId(),
                result.requestedAmount(),
                result.approvedAmount(),
                result.pgProvider(),
                result.pgOrderId(),
                result.pgPaymentKey(),
                result.chargeStatus(),
                result.requestedAt(),
                result.approvedAt(),
                result.failedAt(),
                result.failureReason(),
                result.hasRefundHistory(),
                result.latestRefund() == null ? null : ChargeRefundSummaryResponse.from(result.latestRefund())
        );
    }
}
