package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeRefundSummaryResult(
        UUID chargeRefundId,
        UUID chargeId,
        Long refundAmount,
        ChargeRefundStatus refundStatus,
        String refundReason,
        LocalDateTime requestedAt,
        LocalDateTime refundedAt,
        LocalDateTime failedAt
) {
}
