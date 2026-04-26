package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.OutboxEvent;
import com.example.product.domain.enumtype.OutboxEventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findAllByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PROCESSING' WHERE e.id = :id AND e.status = 'PENDING'")
    int changeToProcessingIfPending(UUID id);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PUBLISHED', e.publishedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.status = 'PROCESSING'")
    int changeToPublishedIfProcessing(UUID id);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PENDING' WHERE e.id = :id AND e.status = 'PROCESSING'")
    int changeToPendingIfProcessing(UUID id);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PENDING' WHERE e.status = 'PROCESSING' AND e.createdAt <= :threshold")
    int recoverStuckProcessingEvents(LocalDateTime threshold);
}
