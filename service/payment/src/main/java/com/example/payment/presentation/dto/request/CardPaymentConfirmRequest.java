package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CardPaymentConfirmRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,
        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,
        @NotNull(message = "결제 금액은 필수입니다.")
        @DecimalMin(value = "1", message = "결제 금액은 최소 1원 이상이어야 합니다.")
        @Digits(integer = 19, fraction = 0, message = "결제 금액은 원 단위 정수여야 합니다.")
        BigDecimal amount
) {
}
