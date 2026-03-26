package com.example.payment.infrastructure.messaging.kafka.contract;

/**
 * 주문 결제 결과 메시지의 처리 상태다.
 */
public enum OrderPaymentResultStatus {
    SUCCESS,
    FAILED
}
