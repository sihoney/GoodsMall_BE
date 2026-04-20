package com.example.payment.infrastructure.client.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentValidationRequest(
        UUID orderId,
        UUID buyerId,
        BigDecimal amount
) {
}
