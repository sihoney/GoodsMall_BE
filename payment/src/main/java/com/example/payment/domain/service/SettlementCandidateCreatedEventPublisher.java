package com.example.payment.domain.service;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;

public interface SettlementCandidateCreatedEventPublisher {

    void publish(SettlementCandidateCreatedEvent event);
}
