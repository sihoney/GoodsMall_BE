package com.example.product.infrastructure.messaging.kafka;

import com.example.product.domain.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRecoveryJob {

    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckProcessingEvents() {
        int recovered = outboxEventRepository.recoverStuckProcessingEvents(LocalDateTime.now().minusMinutes(5));
        if (recovered > 0) {
            log.warn("Product outbox stuck PROCESSING 이벤트 복구: count={}", recovered);
        }
    }
}
