package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ChargeConfirmRequest(
        @NotNull(message = "chargeId is required.")
        UUID chargeId,
        @NotBlank(message = "paymentKey is required.")
        String paymentKey,
        @NotBlank(message = "orderId is required.")
        String orderId,
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount
) {
}
