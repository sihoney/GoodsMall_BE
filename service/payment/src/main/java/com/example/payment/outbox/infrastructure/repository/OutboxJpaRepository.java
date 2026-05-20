package com.example.payment.outbox.infrastructure.repository;

import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.enumtype.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OutboxJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'PROCESSING' WHERE e.id = :id AND e.status = 'PENDING'")
    int changeToProcessingIfPending(UUID id);

    @Modifying
    @Query("""
            UPDATE OutboxEvent e
            SET e.status = 'PUBLISHED',
                e.publishedAt = CURRENT_TIMESTAMP,
                e.lastErrorMessage = null
            WHERE e.id = :id AND e.status = 'PROCESSING'
            """)
    int changeToPublishedIfProcessing(UUID id);

    @Modifying
    @Query("""
            UPDATE OutboxEvent e
            SET e.status = 'PENDING'
            WHERE e.status = 'PROCESSING'
              AND e.createdAt < :threshold
            """)
    int revertStuckProcessingToPending(LocalDateTime threshold);
}
