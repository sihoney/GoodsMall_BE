package com.example.payment.infrastructure.client.dto.request;

import java.util.UUID;

public record OrderPaymentValidationRequest(
        UUID orderId,
        UUID buyerId,
        Long amount
) {
}
