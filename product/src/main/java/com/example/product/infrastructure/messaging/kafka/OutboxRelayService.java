package com.example.product.infrastructure.messaging.kafka;

import com.example.product.application.event.OutboxEventPendingTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxProcessor outboxProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventPendingTrigger trigger) {
        outboxProcessor.processOutbox();
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        outboxProcessor.processOutbox();
    }
}
