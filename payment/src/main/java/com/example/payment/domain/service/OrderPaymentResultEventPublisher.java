package com.example.payment.domain.service;

import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;

public interface OrderPaymentResultEventPublisher {

    void publish(OrderPaymentResultMessage event);
}
