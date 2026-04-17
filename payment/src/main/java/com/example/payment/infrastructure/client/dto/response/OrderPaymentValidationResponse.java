package com.example.payment.infrastructure.client.dto.response;

import java.util.List;

public record OrderPaymentValidationResponse(
        List<OrderPaymentValidationOrderItemResponse> orderItems
) {
}
