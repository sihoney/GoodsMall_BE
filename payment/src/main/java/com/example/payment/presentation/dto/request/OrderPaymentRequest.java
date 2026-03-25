package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaymentRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,

        @NotNull(message = "buyerMemberId is required.")
        UUID buyerMemberId,

        @NotNull(message = "orderAmount is required.")
        @Positive(message = "orderAmount must be positive.")
        Long orderAmount,

        @NotNull(message = "sellerReceivableAmount is required.")
        @Positive(message = "sellerReceivableAmount must be positive.")
        Long sellerReceivableAmount,

        LocalDateTime releaseAt
) {
}
