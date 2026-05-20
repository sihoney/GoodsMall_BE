package com.example.payment.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CardPaymentConfirmOrderItemCommand(
        UUID orderItemId,
        BigDecimal amount
) {
}
