package com.example.order.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelay = 5000)
    public void scheduledRelay() {
        outboxProcessor.process();
    }
}
