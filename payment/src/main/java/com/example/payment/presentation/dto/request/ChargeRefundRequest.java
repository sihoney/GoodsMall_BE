package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChargeRefundRequest(
        @NotBlank(message = "refundReason is required.")
        String refundReason
) {
}
