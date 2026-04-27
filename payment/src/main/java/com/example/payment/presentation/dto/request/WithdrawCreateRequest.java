package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawCreateRequest(
        @NotNull(message = "금액은 필수입니다.")
        @DecimalMin(value = "0.01", message = "금액은 0보다 커야 합니다.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount,

        @NotBlank(message = "계좌번호는 필수입니다.")
        String bankAccount,

        @NotBlank(message = "예금주는 필수입니다.")
        String accountHolder
) {
}
