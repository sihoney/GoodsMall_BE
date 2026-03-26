package com.example.payment.domain.service;

import java.time.LocalDateTime;

public interface TossPaymentGateway {

    TossPaymentConfirmation confirm(String paymentKey, String orderId, Long amount);

    TossPaymentCancellation cancel(String paymentKey, String cancelReason, Long cancelAmount);

    record TossPaymentConfirmation(
            String paymentKey,
            String orderId,
            Long approvedAmount,
            LocalDateTime approvedAt
    ) {
    }

    record TossPaymentCancellation(
            String paymentKey,
            Long canceledAmount,
            LocalDateTime canceledAt
    ) {
    }
}
