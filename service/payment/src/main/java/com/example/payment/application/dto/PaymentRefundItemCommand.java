package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemCommand(
        UUID orderItemId,
        BigDecimal refundAmount
) {
}
