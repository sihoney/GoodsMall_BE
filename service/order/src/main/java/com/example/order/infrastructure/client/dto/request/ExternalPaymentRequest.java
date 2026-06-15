package com.example.order.infrastructure.client.dto.request;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExternalPaymentRequest(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant requestedAt,
        List<ExternalOrderLineRequest> orderLines
) {
}
