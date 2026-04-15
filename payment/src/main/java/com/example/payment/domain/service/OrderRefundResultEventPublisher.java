package com.example.payment.domain.service;

import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultMessage;

public interface OrderRefundResultEventPublisher {

    void publish(OrderRefundResultMessage event);
}
