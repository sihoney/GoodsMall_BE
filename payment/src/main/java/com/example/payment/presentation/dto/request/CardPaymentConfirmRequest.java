package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CardPaymentConfirmRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,
        @NotBlank(message = "paymentKey is required.")
        String paymentKey,
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount
) {
}
