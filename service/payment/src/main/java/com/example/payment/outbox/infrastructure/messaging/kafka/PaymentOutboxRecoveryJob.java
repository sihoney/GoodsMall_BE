package com.example.payment.outbox.infrastructure.messaging.kafka;

import com.example.payment.outbox.domain.repository.OutboxRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ?ㅻ옒 癒몃Ц PROCESSING outbox ?대깽?몃? PENDING?쇰줈 蹂듦뎄?쒕떎.
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
