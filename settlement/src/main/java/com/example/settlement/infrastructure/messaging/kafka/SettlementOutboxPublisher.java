package com.example.settlement.infrastructure.messaging.kafka;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * settlement outbox 릴레이 스케줄러다.
 */
@Component
public class SettlementOutboxPublisher {

    private final SettlementOutboxProcessor settlementOutboxProcessor;

    public SettlementOutboxPublisher(SettlementOutboxProcessor settlementOutboxProcessor) {
        this.settlementOutboxProcessor = settlementOutboxProcessor;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        settlementOutboxProcessor.process();
    }
}
