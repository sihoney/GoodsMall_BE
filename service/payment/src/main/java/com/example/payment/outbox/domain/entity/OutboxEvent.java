package com.example.payment.outbox.domain.entity;

import com.example.payment.outbox.domain.enumtype.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "outbox_events", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    private static final int MAX_RETRY_COUNT = 5;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "topic", nullable = false, updatable = false, length = 200)
    private String topic;

    @Column(name = "event_type", nullable = false, updatable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, updatable = false, length = 255)
    private String aggregateId;

    @Column(name = "trace_id", length = 255)
    private String traceId;

    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    private OutboxEvent(
            UUID id,
            String topic,
            String eventType,
            String aggregateId,
            String traceId,
            String payload,
            LocalDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.topic = Objects.requireNonNull(topic);
        this.eventType = Objects.requireNonNull(eventType);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.traceId = traceId;
        this.payload = Objects.requireNonNull(payload);
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.lastErrorMessage = null;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.publishedAt = null;
    }

    public static OutboxEvent create(
            String topic,
            String eventType,
            String aggregateId,
            String traceId,
            String payload
    ) {
        return new OutboxEvent(
                UUID.randomUUID(),
                topic,
                eventType,
                aggregateId,
                traceId,
                payload,
                LocalDateTime.now()
        );
    }

    public void revertToPending(String lastErrorMessage) {
        this.retryCount++;
        this.lastErrorMessage = lastErrorMessage;
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = OutboxStatus.FAILED;
            return;
        }
        this.status = OutboxStatus.PENDING;
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Objects.requireNonNull(publishedAt);
        this.lastErrorMessage = null;
    }
}
