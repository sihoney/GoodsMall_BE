package com.example.payment.charge.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargeConfirmCommand(
        UUID chargeId,
        String paymentKey,
        String pgOrderId,
        BigDecimal amount
) {
}
