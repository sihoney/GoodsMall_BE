package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChargeRefundResult(
        UUID chargeId,
        ChargeRefundStatus refundStatus,
        Long refundedAmount,
        Long walletBalance,
        LocalDateTime refundedAt
) {
}
