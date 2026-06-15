package com.example.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceMemberEventTest {

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Mock
    private NotificationPushService notificationPushService;

    @Mock
    private NotificationMetricsRecorder notificationMetricsRecorder;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationJpaRepository,
                notificationPushService,
                notificationMetricsRecorder
        );
    }

    @Test
    void createSellerPromotedNotification_savesAndPushes() {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 14, 0);
        when(notificationJpaRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createSellerPromotedNotification(eventId, "trace-id", memberId, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_PROMOTED);
        assertThat(saved.getTitle()).isEqualTo("판매자 자격이 완료되었어요");
        assertThat(saved.getContent()).isEqualTo("이제 판매자 기능을 이용할 수 있어요.");
        verify(notificationPushService).push(any());
    }

    @Test
    void createAccountVerificationExpiredNotification_savesAndPushes() {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 14, 5);
        when(notificationJpaRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAccountVerificationExpiredNotification(eventId, "trace-id", memberId, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.ACCOUNT_VERIFICATION_EXPIRED);
        assertThat(saved.getTitle()).isEqualTo("계좌 인증이 만료되었어요");
        verify(notificationPushService).push(any());
    }

    @Test
    void createAccountVerificationFailedNotification_savesAndPushes() {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 14, 10);
        when(notificationJpaRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAccountVerificationFailedNotification(eventId, "trace-id", memberId, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.ACCOUNT_VERIFICATION_FAILED);
        assertThat(saved.getTitle()).isEqualTo("계좌 인증에 실패했어요");
        verify(notificationPushService).push(any());
    }

    @Test
    void createMemberOauthLinkedNotification_savesInboxOnly() {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 14, 15);
        when(notificationJpaRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createMemberOauthLinkedNotification(eventId, "trace-id", memberId, "KAKAO", occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.MEMBER_OAUTH_LINKED);
        assertThat(saved.getTitle()).isEqualTo("소셜 계정 연동이 완료되었어요");
        assertThat(saved.getContent()).isEqualTo("KAKAO 계정 연동이 완료되었어요.");
        org.mockito.Mockito.verifyNoInteractions(notificationPushService);
    }
}
