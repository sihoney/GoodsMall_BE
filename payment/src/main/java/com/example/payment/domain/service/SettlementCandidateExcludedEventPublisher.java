package com.example.payment.domain.service;

import com.example.payment.application.event.SettlementCandidateExcludedEvent;

public interface SettlementCandidateExcludedEventPublisher {

    void publish(SettlementCandidateExcludedEvent event);
}
