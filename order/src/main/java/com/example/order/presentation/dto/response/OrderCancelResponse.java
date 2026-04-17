package com.example.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelResponse(
        UUID orderId,
        BigDecimal refundedAmount,
        LocalDateTime canceledAt
) {
}
