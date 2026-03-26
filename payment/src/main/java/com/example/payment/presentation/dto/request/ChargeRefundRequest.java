package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 충전 환불 API의 입력 DTO다.
 */
public record ChargeRefundRequest(
        @NotBlank(message = "refundReason is required.")
        String refundReason
) {
}
