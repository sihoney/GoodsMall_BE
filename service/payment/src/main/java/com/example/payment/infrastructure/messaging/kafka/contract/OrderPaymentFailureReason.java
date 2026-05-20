package com.example.payment.infrastructure.messaging.kafka.contract;

/**
 * 주문 결제 실패 결과에서 사용하는 표준 실패 사유 코드다.
 */
public enum OrderPaymentFailureReason {
    DUPLICATE_ORDER_PAYMENT,
    WALLET_NOT_FOUND,
    INSUFFICIENT_BALANCE,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
