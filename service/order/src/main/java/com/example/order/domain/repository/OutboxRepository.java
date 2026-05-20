package com.example.order.domain.repository;

import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.enumtype.OutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findByStatus(OutboxStatus status);

    Optional<OutboxEvent> findById(UUID id);

    int changeToProcessingIfPending(UUID id);

    int changeToPublishedIfProcessing(UUID id);

    int revertStuckProcessingToPending(LocalDateTime threshold);
}
