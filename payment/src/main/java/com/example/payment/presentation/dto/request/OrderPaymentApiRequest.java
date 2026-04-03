package com.example.payment.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 주문 결제 API 요청 DTO다.
 * 기존 Kafka OrderPaymentRequested 계약을 HTTP body 형태로 옮긴다.
 */
public record OrderPaymentApiRequest(

        @NotNull(message = "orderId is required.")
        UUID orderId,

        @NotNull(message = "buyerId is required.")
        UUID buyerId,

        @NotNull(message = "totalPrice is required.")
        @Positive(message = "totalPrice must be positive.")
        BigDecimal totalPrice,

        @NotNull(message = "requestedAt is required.")
        Instant requestedAt,

        @NotEmpty(message = "orderLines must not be empty.")
        List<@Valid OrderPaymentApiOrderLineRequest> orderLines
) {
}
