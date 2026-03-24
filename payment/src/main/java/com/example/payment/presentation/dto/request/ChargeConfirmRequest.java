package com.example.payment.presentation.dto.request;

import java.util.UUID;

public record ChargeConfirmRequest(
        UUID chargeId,
        String paymentKey,
        String pgOrderId,
        Long amount
) {
}
