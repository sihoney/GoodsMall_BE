package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.entity.OutboxEvent;
import com.example.payment.domain.enumtype.OutboxStatus;
import com.example.payment.domain.repository.OutboxRepository;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * payment outbox의 PENDING 이벤트를 Kafka로 릴레이한다.
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
            return "Kafka 발행 실패 원인을 확인할 수 없습니다.";
        }
        return exception.getMessage();
    }
}
