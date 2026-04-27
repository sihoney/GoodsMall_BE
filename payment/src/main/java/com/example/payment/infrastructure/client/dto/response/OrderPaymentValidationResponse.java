package com.example.payment.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderPaymentValidationResponse(
        BigDecimal totalAmount,
        List<OrderPaymentValidationOrderItemResponse> items
) {
}
