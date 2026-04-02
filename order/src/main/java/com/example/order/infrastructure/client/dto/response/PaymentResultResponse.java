package com.example.order.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResultResponse(
        UUID orderId,
        BigDecimal amount,
        String status,
        String reasonCode) {
}
