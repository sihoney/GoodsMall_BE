package com.example.payment.domain.service;

import com.example.payment.infrastructure.messaging.kafka.contract.CardConfirmResultMessage;

public interface CardConfirmResultEventPublisher {

    void publish(CardConfirmResultMessage event);
}
