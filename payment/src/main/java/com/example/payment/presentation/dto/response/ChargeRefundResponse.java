package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeRefundResult;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeRefundResponse(
        UUID chargeId,
        ChargeRefundStatus refundStatus,
        Long refundedAmount,
        Long walletBalance,
        LocalDateTime refundedAt
) {

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
