package com.example.payment.payment.application.dto;

import com.example.payment.payment.domain.enumtype.PaymentRefundType;
import java.util.List;
import java.util.UUID;

public record SellerRefundCommand(
        UUID orderId,
        UUID sellerMemberId,
        UUID orderCancelRequestId,
        PaymentRefundType refundType,
        String reason,
        List<UUID> orderItemIds
) {
}
