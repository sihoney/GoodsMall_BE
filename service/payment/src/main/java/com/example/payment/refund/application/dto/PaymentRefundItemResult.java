package com.example.payment.refund.application.dto;

import com.example.payment.refund.domain.enumtype.PaymentRefundItemStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemResult(
        UUID orderItemId,
        PaymentRefundItemStatus status,
        BigDecimal refundAmount
) {
}
