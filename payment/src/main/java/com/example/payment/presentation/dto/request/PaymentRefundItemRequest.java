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
        @DecimalMin(value = "1", message = "refundAmount must be at least 1 KRW.")
        @Digits(integer = 19, fraction = 0, message = "refundAmount must be integer KRW.")
        BigDecimal refundAmount
) {
}
