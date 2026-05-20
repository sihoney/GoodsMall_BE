package com.example.payment.orderpayment.infrastructure.client.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentValidationRequest(
        UUID buyerId,
        BigDecimal amount
) {
}
