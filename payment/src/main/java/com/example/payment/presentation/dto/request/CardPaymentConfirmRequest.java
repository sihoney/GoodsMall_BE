package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CardPaymentConfirmRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,
        @NotBlank(message = "paymentKey is required.")
        String paymentKey,
        @NotNull(message = "amount is required.")
        @DecimalMin(value = "1", message = "amount must be at least 1 KRW.")
        @Digits(integer = 19, fraction = 0, message = "amount must be integer KRW.")
        BigDecimal amount
) {
}
