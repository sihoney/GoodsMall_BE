package com.example.payment.payment.application.dto;

import com.example.payment.payment.domain.enumtype.PaymentRefundStatus;
import com.example.payment.payment.domain.enumtype.PaymentRefundType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentRefundResult(
        UUID refundId,
        UUID orderId,
        UUID orderCancelRequestId,
        PaymentRefundStatus refundStatus,
        PaymentRefundType refundType,
        BigDecimal totalRefundAmount,
        List<PaymentRefundItemResult> itemResults,
        LocalDateTime processedAt
) {
}
