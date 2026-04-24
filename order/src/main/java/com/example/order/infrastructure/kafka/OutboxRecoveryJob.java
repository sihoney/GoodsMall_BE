package com.example.order.infrastructure.kafka;

import com.example.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRecoveryJob {

    private final OutboxRepository outboxRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckProcessing() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        int count = outboxRepository.revertStuckProcessingToPending(threshold);
        if (count > 0) {
            log.warn("PROCESSING stuck 이벤트 복구. count={}", count);
        }
    }
}
