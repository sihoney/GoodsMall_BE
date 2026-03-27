package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * charge refund 목록 단건 응답 DTO다.
 */
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

    /**
     * application 결과를 presentation 응답으로 변환한다.
     */
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
