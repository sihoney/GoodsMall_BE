package com.example.payment.payment.application.dto;

import com.example.payment.payment.domain.enumtype.PaymentRefundType;
import java.util.List;
import java.util.UUID;

public record PaymentRefundCommand(
        UUID orderId,
        UUID buyerMemberId,
        UUID orderCancelRequestId,
        PaymentRefundType refundType,
        String reason,
        List<PaymentRefundItemCommand> items
) {
}
