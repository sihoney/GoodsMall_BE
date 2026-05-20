package com.example.payment.wallet.application.dto;

public record ChargeConfirmFailureCommand(
        String orderId,
        String failureCode,
        String failureMessage
) {
}
