package com.todaylunch.auction.infrastructure.messaging.kafka;

import com.todaylunch.auction.application.event.OutboxEventPendingTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxProcessor outboxProcessor;

    // 트랜잭션 커밋 직후 즉시 실행 — 지연 없이 Kafka로 발행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventPendingTrigger trigger) {
        outboxProcessor.processOutbox();
    }

    // 누락된 PENDING 이벤트 백업 처리 (5초 폴링)
    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        outboxProcessor.processOutbox();
    }
}
