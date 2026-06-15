package com.todaylunch.auction.domain.entity;

import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
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
@Table(name = "outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, updatable = false, length = 50)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, updatable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, updatable = false, length = 200)
    private String topic;

    @Column(name = "partition_key", length = 100)
    private String partitionKey;

    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    private OutboxEvent(
            UUID id,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            String partitionKey,
            String payload,
            LocalDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.aggregateType = Objects.requireNonNull(aggregateType);
        this.eventType = Objects.requireNonNull(eventType);
        this.topic = Objects.requireNonNull(topic);
        this.partitionKey = partitionKey;
        this.payload = Objects.requireNonNull(payload);
        this.status = OutboxEventStatus.PENDING;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.publishedAt = null;
    }

    public static OutboxEvent create(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            String partitionKey,
            String payload
    ) {
        return new OutboxEvent(
                UUID.randomUUID(),
                aggregateId,
                aggregateType,
                eventType,
                topic,
                partitionKey,
                payload,
                LocalDateTime.now()
        );
    }

    public void changePublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void revertToPending() {
        this.status = OutboxEventStatus.PENDING;
    }
}
