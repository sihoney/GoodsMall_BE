package com.example.order.infrastructure.client.dto.request;

import com.example.order.application.port.dto.request.PaymentRefundLineRequest;

import java.util.UUID;

public record ExternalPaymentRefundLineRequest(
        UUID orderItemId,
        Long refundAmount
) {
    public static ExternalPaymentRefundLineRequest from(PaymentRefundLineRequest request) {
        return new ExternalPaymentRefundLineRequest(
                request.orderItemId(),
                request.refundAmount().longValueExact()
        );
    }
}
