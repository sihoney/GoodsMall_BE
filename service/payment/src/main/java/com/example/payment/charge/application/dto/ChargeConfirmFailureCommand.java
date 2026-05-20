package com.example.payment.charge.application.dto;

public record ChargeConfirmFailureCommand(
        String orderId,
        String failureCode,
        String failureMessage
) {
}
