package com.example.payment.orderpayment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentValidationCommand(
        UUID orderId,
        UUID buyerId,
        BigDecimal amount
) {
}
