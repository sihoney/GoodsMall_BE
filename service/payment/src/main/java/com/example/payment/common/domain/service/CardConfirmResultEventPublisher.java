package com.example.payment.common.domain.service;

import com.example.payment.common.infrastructure.messaging.kafka.contract.CardConfirmResultMessage;

public interface CardConfirmResultEventPublisher {

    void publish(CardConfirmResultMessage event);
}
