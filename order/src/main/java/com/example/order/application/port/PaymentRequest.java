package com.example.order.application.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentRequest(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant requestedAt,
        List<PaymentRequestOrderLine> paymentRequestOrderLines
) {
}
