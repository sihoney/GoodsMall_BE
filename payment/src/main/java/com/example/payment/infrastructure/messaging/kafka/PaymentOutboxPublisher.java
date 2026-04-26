package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.OutboxEventPendingTrigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * payment outbox 릴레이 스케줄러다.
 */
@Component
public class PaymentOutboxPublisher {

    private final PaymentOutboxProcessor paymentOutboxProcessor;

    public PaymentOutboxPublisher(PaymentOutboxProcessor paymentOutboxProcessor) {
        this.paymentOutboxProcessor = paymentOutboxProcessor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventPendingTrigger trigger) {
        paymentOutboxProcessor.process();
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        paymentOutboxProcessor.process();
    }
}
