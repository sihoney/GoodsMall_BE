package com.example.payment.domain.service;

import java.time.LocalDateTime;

public interface TossPaymentGateway {

    TossPaymentConfirmation confirm(String paymentKey, String orderId, Long amount);

    record TossPaymentConfirmation(
            String paymentKey,
            String orderId,
            Long approvedAmount,
            LocalDateTime approvedAt
    ) {
    }
}
