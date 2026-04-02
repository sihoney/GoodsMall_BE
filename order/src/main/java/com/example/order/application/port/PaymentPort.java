package com.example.order.application.port;

import com.example.order.domain.enumtype.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentPort {

    PaymentResult requestPayment(PaymentRequest request);

    record PaymentRequest(
            UUID orderId,
            UUID buyerId,
            BigDecimal totalPrice,
            Instant requestedAt,
            List<OrderLine> orderLines
    ) {
    }

    record OrderLine(
            UUID orderItemId,
            UUID sellerId,
            BigDecimal unitPriceSnapshot,
            int quantity,
            BigDecimal lineTotalPrice) {
    }

    record PaymentResult(
            UUID orderId,
            BigDecimal paidAmount,
            PaymentStatus status,
            String reasonCode
    ) {
    }
}
