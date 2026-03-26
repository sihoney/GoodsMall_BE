package com.example.payment.application.dto;

import java.util.UUID;

public record ChargeConfirmCommand(
        UUID chargeId,
        String paymentKey,
        String pgOrderId,
        Long amount
) {
}
