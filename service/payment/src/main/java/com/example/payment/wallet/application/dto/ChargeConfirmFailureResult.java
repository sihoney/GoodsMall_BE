package com.example.payment.wallet.application.dto;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeConfirmFailureResult(
        UUID chargeId,
        ChargeStatus chargeStatus,
        String orderId,
        String failureReason,
        LocalDateTime failedAt
) {
}
