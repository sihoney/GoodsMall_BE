package com.todaylunch.auction.infrastructure.messaging.kafka;

import com.todaylunch.auction.application.event.OutboxEventPendingTrigger;
import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
import com.todaylunch.auction.domain.repository.OutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 트랜잭션 커밋 직후 즉시 실행 — 지연 없이 Kafka로 발행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventPendingTrigger trigger) {
        processOutbox();
    }

    // 누락된 PENDING 이벤트 백업 처리 (5초 폴링)
    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        processOutbox();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOutbox() {
        List<OutboxEvent> pending = outboxEventRepository.findAllByStatus(OutboxEventStatus.PENDING);

        for (OutboxEvent event : pending) {
            int updated = outboxEventRepository.changeToPublishedIfPending(event.getId());
            if (updated == 0) {
                continue;
            }
            try {
                kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), event.getPayload()).get();
                log.debug("Outbox Kafka 발행 성공: id={}, topic={}", event.getId(), event.getTopic());
            } catch (Exception e) {
                log.error("Outbox Kafka 발행 실패: id={}, topic={}", event.getId(), event.getTopic(), e);
                event.revertToPending();
            }
        }
    }
}
