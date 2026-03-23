package com.example.notification.domain.entity;

import com.example.notification.domain.enumtype.NotificationReferenceType;
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

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Notification(
            UUID notificationId,
            UUID memberId,
            NotificationType type,
            String title,
            String content,
            UUID referenceId,
            NotificationReferenceType referenceType,
            boolean read,
            LocalDateTime createdAt
    ) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.memberId = Objects.requireNonNull(memberId);
        this.type = Objects.requireNonNull(type);
        this.title = Objects.requireNonNull(title);
        this.content = Objects.requireNonNull(content);
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.read = read;
        this.createdAt = Objects.requireNonNull(createdAt);
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
        return new Notification(notificationId, memberId, type, title, content, referenceId, referenceType, false, createdAt);
    }

    public void markAsRead() {
        this.read = true;
    }
}
