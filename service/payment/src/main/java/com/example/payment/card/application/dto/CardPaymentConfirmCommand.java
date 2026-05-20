package com.example.payment.card.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CardPaymentConfirmCommand(
        UUID buyerId,
        UUID orderId,
        String paymentKey,
        BigDecimal amount
) {
}
