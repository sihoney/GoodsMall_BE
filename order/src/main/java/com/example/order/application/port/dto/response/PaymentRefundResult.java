package com.example.order.application.port.dto.response;

import com.example.order.domain.enumtype.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundResult(
        UUID orderId,
        BigDecimal refundedAmount,
        PaymentStatus status,
        Instant canceledAt,
        String failReason
) {
}