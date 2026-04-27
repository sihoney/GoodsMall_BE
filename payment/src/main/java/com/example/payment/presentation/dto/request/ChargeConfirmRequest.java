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
        @NotNull(message = "충전 ID는 필수입니다.")
        UUID chargeId,
        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,
        @NotNull(message = "금액은 필수입니다.")
        @DecimalMin(value = "1", message = "금액은 최소 1원 이상이어야 합니다.")
        @Digits(integer = 19, fraction = 0, message = "금액은 원 단위 정수여야 합니다.")
        BigDecimal amount
) {
}
