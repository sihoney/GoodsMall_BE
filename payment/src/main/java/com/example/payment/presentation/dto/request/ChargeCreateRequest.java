package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 충전 요청 생성 API의 입력 DTO다.
 */
public record ChargeCreateRequest(
        @NotNull(message = "amount is required.")
        @DecimalMin(value = "0.01", message = "amount must be positive.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount
) {
}
