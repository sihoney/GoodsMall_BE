package com.example.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
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
class NotificationServiceTest {

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Mock
    private NotificationPushService notificationPushService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationJpaRepository, notificationPushService);
    }

    @Test
    void createAutoPurchaseConfirmedNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAutoPurchaseConfirmedNotification(eventId, null, orderId, buyerMemberId, confirmedAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(saved.getMemberId()).isEqualTo(buyerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.AUTO_PURCHASE_CONFIRMED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        assertThat(saved.getCreatedAt()).isEqualTo(confirmedAt);
        assertThat(saved.isRead()).isFalse();
        verify(notificationPushService).push(any());
    }

    @Test
    void createAutoPurchaseConfirmedNotification_throwsWhenBuyerMemberIdIsNull() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        assertThatThrownBy(() ->
                notificationService.createAutoPurchaseConfirmedNotification(eventId, null, orderId, null, confirmedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("memberId is required.");
    }

    @Test
    void createOrderPaymentSucceededNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderPaymentSucceededNotification(eventId, null, orderId, buyerMemberId, 30000L, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(saved.getMemberId()).isEqualTo(buyerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_PAYMENT_SUCCEEDED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        assertThat(saved.getCreatedAt()).isEqualTo(occurredAt);
        verify(notificationPushService).push(any());
    }

    @Test
    void createOrderPaymentFailedNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderPaymentFailedNotification(
                eventId,
                null,
                orderId,
                buyerMemberId,
                OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_PAYMENT_FAILED);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        verify(notificationPushService).push(any());
    }

    @Test
    void createSellerSettlementPayoutSucceededNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime processedAt = LocalDateTime.of(2026, 3, 29, 10, 20, 4);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createSellerSettlementPayoutSucceededNotification(
                eventId,
                null,
                settlementId,
                sellerMemberId,
                180000L,
                processedAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(saved.getMemberId()).isEqualTo(sellerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_SETTLEMENT_PAYOUT_SUCCEEDED);
        assertThat(saved.getReferenceId()).isEqualTo(settlementId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.SETTLEMENT);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        assertThat(saved.getCreatedAt()).isEqualTo(processedAt);
        verify(notificationPushService).push(any());
    }

    @Test
    void createSellerSettlementPayoutFailedNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime processedAt = LocalDateTime.of(2026, 3, 29, 10, 20, 4);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createSellerSettlementPayoutFailedNotification(
                eventId,
                null,
                settlementId,
                sellerMemberId,
                PayoutFailureReason.WALLET_NOT_FOUND,
                processedAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.STORED);
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_SETTLEMENT_PAYOUT_FAILED);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        verify(notificationPushService).push(any());
    }
}
