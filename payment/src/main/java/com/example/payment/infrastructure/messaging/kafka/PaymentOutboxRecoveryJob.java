package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.repository.OutboxRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 오래 머문 PROCESSING outbox 이벤트를 PENDING으로 복구한다.
 */
@Component
public class PaymentOutboxRecoveryJob {

    private final OutboxRepository outboxRepository;

    public PaymentOutboxRecoveryJob(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckProcessing() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        outboxRepository.revertStuckProcessingToPending(threshold);
    }
}
