package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 충전 확인 API의 입력 DTO다.
 * PG 확인 결과와 charge 요청 정보를 함께 담는다.
 */
public record ChargeConfirmRequest(
        @NotNull(message = "chargeId is required.")
        UUID chargeId,
        @NotBlank(message = "paymentKey is required.")
        String paymentKey,
        @NotBlank(message = "orderId is required.")
        String orderId,
        @NotNull(message = "amount is required.")
        @DecimalMin(value = "1", message = "amount must be at least 1 KRW.")
        @Digits(integer = 19, fraction = 0, message = "amount must be integer KRW.")
        BigDecimal amount
) {
}
