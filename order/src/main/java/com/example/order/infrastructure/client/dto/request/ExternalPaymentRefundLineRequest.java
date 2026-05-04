package com.example.order.infrastructure.client.dto.request;

import com.example.order.application.port.dto.request.PaymentRefundLineRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record ExternalPaymentRefundLineRequest(
        UUID orderItemId,
        BigDecimal refundAmount
) {
    public static ExternalPaymentRefundLineRequest from(PaymentRefundLineRequest request) {
        // payment 서비스는 원 단위 정수만 허용하므로 scale=0으로 정규화
        BigDecimal amount = request.refundAmount() == null
                ? null
                : request.refundAmount().setScale(0, RoundingMode.HALF_UP);
        return new ExternalPaymentRefundLineRequest(
                request.orderItemId(),
                amount
        );
    }
}
