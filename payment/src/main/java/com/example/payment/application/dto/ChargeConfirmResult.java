package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeConfirmResult(
        UUID chargeId,
        ChargeStatus chargeStatus,
        Long approvedAmount,
        Long walletBalance,
        LocalDateTime approvedAt
) {
}
