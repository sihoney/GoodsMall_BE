package com.example.order.infrastructure.kafka;

import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.enumtype.OutboxStatus;
import com.example.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            int updated = outboxRepository.changeToProcessingIfPending(event.getId());
            if (updated == 0) {
                continue;
            }
            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> handlePublishResult(event, ex));
        }
    }

    private void handlePublishResult(OutboxEvent event, Throwable ex) {
        if (ex != null) {
            log.error("Outbox 이벤트 발행 실패. id={}, topic={}", event.getId(), event.getTopic(), ex);
            transactionTemplate.execute(status -> {
                outboxRepository.findById(event.getId())
                        .ifPresent(OutboxEvent::revertToPending);
                return null;
            });
        } else {
            transactionTemplate.execute(status -> {
                outboxRepository.changeToPublishedIfProcessing(event.getId());
                return null;
            });
            log.info("Outbox 이벤트 발행 성공. id={}, topic={}", event.getId(), event.getTopic());
        }
    }
}
