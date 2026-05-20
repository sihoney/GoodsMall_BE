package com.example.payment.refund.application.dto;

import com.example.payment.refund.domain.enumtype.PaymentRefundType;
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
