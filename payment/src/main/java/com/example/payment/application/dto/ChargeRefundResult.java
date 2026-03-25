package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeRefundResult(
        UUID chargeId,
        ChargeStatus chargeStatus,
        Long refundedAmount,
        Long walletBalance,
        LocalDateTime refundedAt
) {
}
