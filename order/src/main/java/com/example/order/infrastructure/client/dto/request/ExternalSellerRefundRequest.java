package com.example.order.infrastructure.client.dto.request;

import com.example.order.domain.enumtype.PaymentRefundType;

import java.util.List;
import java.util.UUID;

public record ExternalSellerRefundRequest(
        UUID orderId,
        UUID orderCancelRequestId,
        PaymentRefundType refundType,
        String reason,
        List<ExternalSellerRefundLineRequest> items
) {
}
