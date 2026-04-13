package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CardPaymentConfirmOrderItemRequest(
        @NotNull(message = "orderItemId is required.")
        UUID orderItemId,
        @NotNull(message = "order item amount is required.")
        @Positive(message = "order item amount must be positive.")
        Long amount
) {
}
