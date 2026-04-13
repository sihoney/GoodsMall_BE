package com.example.payment.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

public record CardPaymentConfirmRequest(
        @NotNull(message = "buyerId is required.")
        UUID buyerId,
        @NotNull(message = "orderId is required.")
        UUID orderId,
        @NotBlank(message = "paymentKey is required.")
        String paymentKey,
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount,
        @NotEmpty(message = "orderItems must not be empty.")
        List<@Valid CardPaymentConfirmOrderItemRequest> orderItems
) {
}
