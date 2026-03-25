package com.example.payment.presentation.dto.response;

import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeConfirmResponse(
        UUID chargeId,
        ChargeStatus chargeStatus,
        Long approvedAmount,
        Long walletBalance,
        LocalDateTime approvedAt
) {

    public static ChargeConfirmResponse from(ChargeConfirmResult result) {
        return new ChargeConfirmResponse(
                result.chargeId(),
                result.chargeStatus(),
                result.approvedAmount(),
                result.walletBalance(),
                result.approvedAt()
        );
    }
}
