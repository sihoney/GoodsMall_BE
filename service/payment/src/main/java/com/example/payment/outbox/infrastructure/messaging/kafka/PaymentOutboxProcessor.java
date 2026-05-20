package com.example.payment.outbox.infrastructure.messaging.kafka;

import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.enumtype.OutboxStatus;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * payment outbox??PENDING ?대깽?몃? Kafka濡?由대젅?댄븳??
 */
@Component
public class PaymentOutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    public PaymentOutboxProcessor(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            TransactionTemplate transactionTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            int updatedCount = outboxRepository.changeToProcessingIfPending(event.getId());
            if (updatedCount == 0) {
                continue;
            }

            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .whenComplete((result, exception) -> handlePublishResult(event, exception));
        }
    }

    private void handlePublishResult(OutboxEvent event, Throwable exception) {
        if (exception != null) {
            transactionTemplate.executeWithoutResult(status -> outboxRepository.findById(event.getId())
                    .ifPresent(outboxEvent -> {
                        outboxEvent.revertToPending(resolveLastErrorMessage(exception));
                        outboxRepository.save(outboxEvent);
                    }));
            return;
        }

        transactionTemplate.executeWithoutResult(status ->
                outboxRepository.changeToPublishedIfProcessing(event.getId()));
    }

    private String resolveLastErrorMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "Kafka 諛쒗뻾 ?ㅽ뙣 ?먯씤???뺤씤?????놁뒿?덈떎.";
        }
        return exception.getMessage();
    }
}
