package com.example.payment.orderpayment.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentValidationOrderItemResponse(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal lineAmount
) {
}
