package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeConfirmFailureResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeConfirmFailureResponse(
        UUID chargeId,
        ChargeStatus chargeStatus,
        String orderId,
        String failureReason,
        LocalDateTime failedAt
) {

    public static ChargeConfirmFailureResponse from(ChargeConfirmFailureResult result) {
        return new ChargeConfirmFailureResponse(
                result.chargeId(),
                result.chargeStatus(),
                result.orderId(),
                result.failureReason(),
                result.failedAt()
        );
    }
}
