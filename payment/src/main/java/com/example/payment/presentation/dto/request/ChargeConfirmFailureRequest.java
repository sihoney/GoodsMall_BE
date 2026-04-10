package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChargeConfirmFailureRequest(
        @NotBlank(message = "orderId is required.")
        String orderId,
        String code,
        @NotBlank(message = "message is required.")
        String message
) {
}
