package com.example.order.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundResultResponse(
        UUID orderId,
        BigDecimal totalRefundAmount,
        String refundStatus,
        Instant processedAt,
        String failReason
) {
}
