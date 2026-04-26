package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.OutboxEvent;
import com.example.product.domain.enumtype.OutboxEventStatus;
import com.example.product.domain.repository.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public List<OutboxEvent> findAllByStatus(OutboxEventStatus status) {
        return jpaRepository.findAllByStatusOrderByCreatedAtAsc(status);
    }

    @Override
    public int changeToProcessingIfPending(UUID id) {
        return jpaRepository.changeToProcessingIfPending(id);
    }

    @Override
    public int changeToPublishedIfProcessing(UUID id) {
        return jpaRepository.changeToPublishedIfProcessing(id);
    }

    @Override
    public int changeToPendingIfProcessing(UUID id) {
        return jpaRepository.changeToPendingIfProcessing(id);
    }

    @Override
    public int recoverStuckProcessingEvents(LocalDateTime threshold) {
        return jpaRepository.recoverStuckProcessingEvents(threshold);
    }
}
