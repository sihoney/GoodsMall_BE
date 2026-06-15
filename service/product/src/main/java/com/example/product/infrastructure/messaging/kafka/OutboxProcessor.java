package com.example.product.infrastructure.messaging.kafka;

import com.example.product.domain.entity.OutboxEvent;
import com.example.product.domain.enumtype.OutboxEventStatus;
import com.example.product.domain.repository.OutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOutbox() {
        List<OutboxEvent> pending = outboxEventRepository.findAllByStatus(OutboxEventStatus.PENDING);

        for (OutboxEvent event : pending) {
            int claimed = outboxEventRepository.changeToProcessingIfPending(event.getId());
            if (claimed == 0) {
                continue;
            }

            try {
                kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), event.getPayload()).get();
                outboxEventRepository.changeToPublishedIfProcessing(event.getId());
                log.debug("Product outbox Kafka 발행 성공: id={}, topic={}", event.getId(), event.getTopic());
            } catch (Exception e) {
                outboxEventRepository.changeToPendingIfProcessing(event.getId());
                log.error("Product outbox Kafka 발행 실패: id={}, topic={}", event.getId(), event.getTopic(), e);
            }
        }
    }
}
