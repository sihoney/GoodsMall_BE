package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChargeCreateRequest(
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount
) {
}
