package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawCreateRequest(
        @NotNull(message = "amount is required.")
        @DecimalMin(value = "0.01", message = "amount must be positive.")
        @Digits(integer = 19, fraction = 2)
        BigDecimal amount,

        @NotBlank(message = "bankAccount is required.")
        String bankAccount,

        @NotBlank(message = "accountHolder is required.")
        String accountHolder
) {
}
