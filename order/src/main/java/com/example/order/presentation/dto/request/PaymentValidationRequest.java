package com.example.order.presentation.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentValidationRequest(
        UUID buyerId,
        BigDecimal amount
) {
}
