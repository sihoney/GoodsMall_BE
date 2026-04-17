package com.example.payment.infrastructure.client.dto.response;

import java.util.UUID;

public record OrderPaymentValidationOrderItemResponse(
        UUID orderItemId,
        UUID sellerId,
        Long lineAmount
) {
}
