package com.example.payment.application.dto;

public record ChargeConfirmFailureCommand(
        String orderId,
        String failureCode,
        String failureMessage
) {
}
