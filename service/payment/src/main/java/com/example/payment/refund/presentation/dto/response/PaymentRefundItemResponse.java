package com.example.payment.refund.presentation.dto.response;

import com.example.payment.refund.application.dto.PaymentRefundItemResult;
import com.example.payment.refund.domain.enumtype.PaymentRefundItemStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundItemResponse(
        UUID orderItemId,
        PaymentRefundItemStatus status,
        BigDecimal refundAmount
) {
    public static PaymentRefundItemResponse from(PaymentRefundItemResult result) {
        return new PaymentRefundItemResponse(
                result.orderItemId(),
                result.status(),
                result.refundAmount()
        );
    }
}
