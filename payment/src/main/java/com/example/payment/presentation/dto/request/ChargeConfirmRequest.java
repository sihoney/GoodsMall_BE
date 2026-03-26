package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * 충전 승인 API의 입력 DTO다.
 * PG 승인 결과와 charge 식별 정보를 함께 전달한다.
 */
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
