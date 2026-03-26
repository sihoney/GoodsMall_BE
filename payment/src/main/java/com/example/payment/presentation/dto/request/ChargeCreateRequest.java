package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 충전 요청 생성 API의 입력 DTO다.
 */
public record ChargeCreateRequest(
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount
) {
}
