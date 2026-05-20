package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.enumtype.OutboxStatus;
import com.example.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxJpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findByStatus(OutboxStatus status) {
        return outboxJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(status);
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return outboxJpaRepository.findById(id);
    }

    @Override
    public int changeToProcessingIfPending(UUID id) {
        return outboxJpaRepository.changeToProcessingIfPending(id);
    }

    @Override
    public int changeToPublishedIfProcessing(UUID id) {
        return outboxJpaRepository.changeToPublishedIfProcessing(id);
    }

    @Override
    public int revertStuckProcessingToPending(LocalDateTime threshold) {
        return outboxJpaRepository.revertStuckProcessingToPending(threshold);
    }
}
