package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.event.OutboxEventPendingTrigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * settlement outbox 릴레이 스케줄러다.
 */
@Component
public class SettlementOutboxPublisher {

    private final SettlementOutboxProcessor settlementOutboxProcessor;

    public SettlementOutboxPublisher(SettlementOutboxProcessor settlementOutboxProcessor) {
        this.settlementOutboxProcessor = settlementOutboxProcessor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventPendingTrigger trigger) {
        settlementOutboxProcessor.process();
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        settlementOutboxProcessor.process();
    }
}
