package com.example.order.infrastructure.kafka;

import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventSaver {

    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(OutboxEvent event) {
        outboxRepository.save(event);
    }
}
