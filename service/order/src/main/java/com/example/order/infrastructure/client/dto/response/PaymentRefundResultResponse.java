package com.example.order.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRefundResultResponse(
        UUID orderId,
        BigDecimal totalRefundAmount,
        String refundStatus,
        LocalDateTime processedAt,
        String failReason
) {
}
