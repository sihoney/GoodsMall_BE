package com.example.payment.application.dto;

import java.util.UUID;

public record OrderPaymentValidationCommand(
        UUID orderId,
        UUID buyerId,
        Long amount
) {
}
