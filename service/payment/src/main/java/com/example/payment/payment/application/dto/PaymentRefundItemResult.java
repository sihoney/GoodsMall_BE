package com.example.payment.payment.application.dto;

import com.example.payment.payment.domain.enumtype.PaymentRefundItemStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemResult(
        UUID orderItemId,
        PaymentRefundItemStatus status,
        BigDecimal refundAmount
) {
}
