package com.example.notification.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void markAsRead_isEffectivelyIdempotent() {
        Notification notification = Notification.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "trace-id",
                UUID.randomUUID(),
                NotificationType.BUYER_SIGNUP_COMPLETED,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                LocalDateTime.of(2026, 4, 20, 10, 0, 0)
        );

        notification.markAsRead();
        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void changeStatus_ignoresSameStatusAsNoOp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 20, 10, 0, 0);
        Notification notification = Notification.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "trace-id",
                UUID.randomUUID(),
                NotificationType.BUYER_SIGNUP_COMPLETED,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                createdAt
        );

        notification.changeStatus(NotificationStatus.STORED, LocalDateTime.of(2026, 4, 20, 10, 5, 0));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(notification.getStatusChangedAt()).isEqualTo(createdAt);
    }

    @Test
    void changeStatus_allowsValidTransitionFromStoredToPushed() {
        Notification notification = Notification.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "trace-id",
                UUID.randomUUID(),
                NotificationType.BUYER_SIGNUP_COMPLETED,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                LocalDateTime.of(2026, 4, 20, 10, 0, 0)
        );

        notification.changeStatus(NotificationStatus.PUSHED, LocalDateTime.of(2026, 4, 20, 10, 1, 0));

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PUSHED);
    }

    @Test
    void changeStatus_rejectsInvalidTransitionFromPushedToFailed() {
        Notification notification = Notification.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "trace-id",
                UUID.randomUUID(),
                NotificationType.BUYER_SIGNUP_COMPLETED,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                LocalDateTime.of(2026, 4, 20, 10, 0, 0)
        );
        notification.changeStatus(NotificationStatus.PUSHED, LocalDateTime.of(2026, 4, 20, 10, 1, 0));

        assertThatThrownBy(() ->
                notification.changeStatus(NotificationStatus.FAILED, LocalDateTime.of(2026, 4, 20, 10, 2, 0))
        ).isInstanceOf(IllegalStateException.class);
    }
}
