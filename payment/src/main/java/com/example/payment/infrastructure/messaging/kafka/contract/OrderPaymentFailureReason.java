package com.example.payment.infrastructure.messaging.kafka.contract;

public enum OrderPaymentFailureReason {
    DUPLICATE_ORDER_PAYMENT,
    WALLET_NOT_FOUND,
    INSUFFICIENT_BALANCE,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
