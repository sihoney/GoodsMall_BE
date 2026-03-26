package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeRefundResult;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 충전 환불 API의 응답 DTO다.
 */
public record ChargeRefundResponse(
        UUID chargeId,
        ChargeRefundStatus refundStatus,
        Long refundedAmount,
        Long walletBalance,
        LocalDateTime refundedAt
) {

    /**
     * application 결과를 presentation 응답 형식으로 변환한다.
     */
    public static ChargeRefundResponse from(ChargeRefundResult result) {
        return new ChargeRefundResponse(
                result.chargeId(),
                result.refundStatus(),
                result.refundedAmount(),
                result.walletBalance(),
                result.refundedAt()
        );
    }
}
