package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge refund 목록 또는 charge 상세 보조 정보에 사용하는 결과다.
 */
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
