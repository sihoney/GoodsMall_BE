package com.example.payment.domain.service;

import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;

/**
 * 주문 결제 결과 메시지 발행 포트를 정의한다.
 */
public interface OrderPaymentResultEventPublisher {

    void publish(OrderPaymentResultMessage event);
}
