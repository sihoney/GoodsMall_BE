package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChargeConfirmFailureRequest(
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,
        String code,
        @NotBlank(message = "실패 메시지는 필수입니다.")
        String message
) {
}
