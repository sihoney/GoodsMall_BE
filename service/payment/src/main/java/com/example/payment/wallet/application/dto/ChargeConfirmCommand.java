package com.example.payment.wallet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargeConfirmCommand(
        UUID chargeId,
        String paymentKey,
        String pgOrderId,
        BigDecimal amount
) {
}
