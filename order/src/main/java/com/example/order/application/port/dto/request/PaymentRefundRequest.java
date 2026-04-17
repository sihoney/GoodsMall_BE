package com.example.order.application.port.dto.request;

import com.example.order.domain.enumtype.PaymentRefundType;

import java.util.List;
import java.util.UUID;

public record PaymentRefundRequest(
        UUID orderId,
        UUID buyerMemberId,
        PaymentRefundType refundType,
        String reason,
        List<PaymentRefundLineRequest> items
) {
}
