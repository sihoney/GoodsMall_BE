package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WithdrawCreateRequest(
        @NotNull(message = "amount is required.")
        @Positive(message = "amount must be positive.")
        Long amount,

        String bankCode,

        @NotBlank(message = "bankAccount is required.")
        String bankAccount,

        @NotBlank(message = "accountHolder is required.")
        String accountHolder
) {
}
