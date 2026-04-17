package com.example.order.infrastructure.client.dto.request;

import com.example.order.domain.enumtype.PaymentRefundType;

import java.util.List;
import java.util.UUID;

public record ExternalPaymentRefundRequest(
        UUID orderId,
        UUID buyerMemberId,
        UUID idempotencyKey,
        PaymentRefundType refundType,
        String reason,
        List<ExternalPaymentRefundLineRequest> items
) {
}
