package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeRefundSummaryResponse(
        UUID chargeRefundId,
        UUID chargeId,
        Long refundAmount,
        ChargeRefundStatus refundStatus,
        String refundReason,
        LocalDateTime requestedAt,
        LocalDateTime refundedAt,
        LocalDateTime failedAt
) {

    public static ChargeRefundSummaryResponse from(ChargeRefundSummaryResult result) {
        return new ChargeRefundSummaryResponse(
                result.chargeRefundId(),
                result.chargeId(),
                result.refundAmount(),
                result.refundStatus(),
                result.refundReason(),
                result.requestedAt(),
                result.refundedAt(),
                result.failedAt()
        );
    }
}
