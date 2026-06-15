package com.example.payment.outbox.infrastructure.repository;

import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.enumtype.OutboxStatus;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxRepositoryImpl(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxJpaRepository.save(event);
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return outboxJpaRepository.findById(id);
    }

    @Override
    public List<OutboxEvent> findByStatus(OutboxStatus status) {
        return outboxJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(status);
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
