package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge 단건 상세 조회 결과다.
 */
public record ChargeDetailResult(
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
        ChargeRefundSummaryResult latestRefund
) {
}
