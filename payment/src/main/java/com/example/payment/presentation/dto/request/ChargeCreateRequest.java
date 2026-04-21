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
        @DecimalMin(value = "1", message = "amount must be at least 1 KRW.")
        @Digits(integer = 19, fraction = 0, message = "amount must be integer KRW.")
        BigDecimal amount
) {
}
