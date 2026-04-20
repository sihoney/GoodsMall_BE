package com.example.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import com.example.notification.infrastructure.sse.NotificationSseEmitterRegistry;
import com.example.notification.presentation.dto.NotificationResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationPushServiceTest {

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Mock
    private NotificationSseEmitterRegistry emitterRegistry;

    @Mock
    private SseEmitter emitter;

    @Mock
    private NotificationMetricsRecorder notificationMetricsRecorder;

    private NotificationPushService notificationPushService;

    @BeforeEach
    void setUp() {
        notificationPushService = new NotificationPushService(
                notificationJpaRepository,
                emitterRegistry,
                notificationMetricsRecorder
        );
    }

    @Test
    void push_updatesStatusToPushedWhenEmitterExists() throws Exception {
        UUID notificationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Notification notification = Notification.create(
                notificationId,
                UUID.randomUUID(),
                "trace-id",
                memberId,
                NotificationType.MEMBER_SIGNED_UP,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                LocalDateTime.of(2026, 4, 16, 10, 0, 0)
        );
        NotificationResponse response = NotificationResponse.from(notification);

        when(emitterRegistry.find(memberId)).thenReturn(java.util.Optional.of(emitter));
        when(notificationJpaRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));

        notificationPushService.push(response);

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(notificationMetricsRecorder).recordPushAttempt(NotificationType.MEMBER_SIGNED_UP);
        verify(notificationMetricsRecorder).recordPushSuccess(NotificationType.MEMBER_SIGNED_UP);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PUSHED);
    }

    @Test
    void push_doesNothingWhenEmitterMissing() {
        UUID notificationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Notification notification = Notification.create(
                notificationId,
                UUID.randomUUID(),
                "trace-id",
                memberId,
                NotificationType.MEMBER_SIGNED_UP,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.STORED,
                LocalDateTime.of(2026, 4, 16, 10, 0, 0)
        );
        NotificationResponse response = NotificationResponse.from(notification);

        when(notificationJpaRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));
        when(emitterRegistry.find(memberId)).thenReturn(java.util.Optional.empty());

        notificationPushService.push(response);

        verify(notificationMetricsRecorder).recordPushAttempt(NotificationType.MEMBER_SIGNED_UP);
        verify(notificationMetricsRecorder).recordEmitterMissing(NotificationType.MEMBER_SIGNED_UP);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.STORED);
    }

    @Test
    void push_skipsWhenNotificationAlreadyPushed() {
        UUID notificationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Notification notification = Notification.create(
                notificationId,
                UUID.randomUUID(),
                "trace-id",
                memberId,
                NotificationType.MEMBER_SIGNED_UP,
                "Welcome",
                "Hello",
                null,
                null,
                NotificationStatus.PUSHED,
                LocalDateTime.of(2026, 4, 16, 10, 0, 0)
        );
        NotificationResponse response = NotificationResponse.from(notification);

        when(notificationJpaRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));

        notificationPushService.push(response);

        verify(notificationMetricsRecorder).recordPushAttempt(NotificationType.MEMBER_SIGNED_UP);
        verify(notificationMetricsRecorder).recordPushSkippedAlreadyPushed(NotificationType.MEMBER_SIGNED_UP);
        verify(emitterRegistry, never()).find(memberId);
        verify(notificationJpaRepository, never()).saveAndFlush(any());
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PUSHED);
    }
}
