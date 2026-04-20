package com.example.notification.domain.entity;

import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;

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
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Column(name = "notification_id", nullable = false, updatable = false)
    private UUID notificationId;

    @Column(name = "event_id", nullable = false, updatable = false, unique = true)
    private UUID eventId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private NotificationReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt;

    private Notification(
            UUID notificationId,
            UUID eventId,
            String traceId,
            UUID memberId,
            NotificationType type,
            String title,
            String content,
            UUID referenceId,
            NotificationReferenceType referenceType,
            NotificationStatus status,
            boolean read,
            LocalDateTime createdAt,
            LocalDateTime statusChangedAt
    ) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.eventId = Objects.requireNonNull(eventId);
        this.traceId = traceId;
        this.memberId = Objects.requireNonNull(memberId);
        this.type = Objects.requireNonNull(type);
        this.title = Objects.requireNonNull(title);
        this.content = Objects.requireNonNull(content);
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.status = Objects.requireNonNull(status);
        this.read = read;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.statusChangedAt = Objects.requireNonNull(statusChangedAt);
    }

    public static Notification create(
            UUID notificationId,
            UUID eventId,
            String traceId,
            UUID memberId,
            NotificationType type,
            String title,
            String content,
            UUID referenceId,
            NotificationReferenceType referenceType,
            NotificationStatus status,
            LocalDateTime createdAt
    ) {
        return new Notification(
                notificationId,
                eventId,
                traceId,
                memberId,
                type,
                title,
                content,
                referenceId,
                referenceType,
                status,
                false,
                createdAt,
                createdAt
        );
    }

    public static Notification create(
            UUID notificationId,
            UUID memberId,
            NotificationType type,
            String title,
            String content,
            UUID referenceId,
            NotificationReferenceType referenceType,
            LocalDateTime createdAt
    ) {
        return create(
                notificationId,
                UUID.randomUUID(),
                null,
                memberId,
                type,
                title,
                content,
                referenceId,
                referenceType,
                NotificationStatus.STORED,
                createdAt
        );
    }

    public void markAsRead() {
        if (this.read) {
            return;
        }
        this.read = true;
    }

    public void changeStatus(NotificationStatus status, LocalDateTime changedAt) {
        NotificationStatus nextStatus = Objects.requireNonNull(status);
        LocalDateTime nextChangedAt = Objects.requireNonNull(changedAt);

        if (this.status == nextStatus) {
            return;
        }

        if (!canTransitionTo(nextStatus)) {
            throw new IllegalStateException(
                    "Invalid notification status transition. currentStatus=" + this.status + ", nextStatus=" + nextStatus
            );
        }

        this.status = nextStatus;
        this.statusChangedAt = nextChangedAt;
    }

    public boolean hasStatus(NotificationStatus status) {
        return this.status == status;
    }

    private boolean canTransitionTo(NotificationStatus nextStatus) {
        return switch (this.status) {
            case RECEIVED -> nextStatus == NotificationStatus.STORED
                    || nextStatus == NotificationStatus.RETRYING
                    || nextStatus == NotificationStatus.FAILED;
            case STORED -> nextStatus == NotificationStatus.PUSHED
                    || nextStatus == NotificationStatus.RETRYING
                    || nextStatus == NotificationStatus.FAILED;
            case RETRYING -> nextStatus == NotificationStatus.RETRYING
                    || nextStatus == NotificationStatus.PUSHED
                    || nextStatus == NotificationStatus.FAILED;
            case FAILED -> nextStatus == NotificationStatus.RETRYING;
            case PUSHED -> false;
        };
    }
}
