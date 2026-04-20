package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemRequest(
        @NotNull(message = "orderItemId is required.")
        UUID orderItemId,

        @NotNull(message = "refundAmount is required.")
        @DecimalMin(value = "0.01", message = "refundAmount must be positive.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal refundAmount
) {
}
