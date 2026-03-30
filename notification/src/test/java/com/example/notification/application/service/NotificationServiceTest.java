package com.example.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
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

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationJpaRepository);
    }

    @Test
    void createAutoPurchaseConfirmedNotification_savesNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAutoPurchaseConfirmedNotification(orderId, buyerMemberId, confirmedAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(buyerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.AUTO_PURCHASE_CONFIRMED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isEqualTo("자동 구매확정 완료");
        assertThat(saved.getContent()).isEqualTo("주문이 자동으로 구매확정 처리되었습니다.");
        assertThat(saved.getCreatedAt()).isEqualTo(confirmedAt);
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void createAutoPurchaseConfirmedNotification_throwsWhenBuyerMemberIdIsNull() {
        UUID orderId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        assertThatThrownBy(() ->
                notificationService.createAutoPurchaseConfirmedNotification(orderId, null, confirmedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("buyerMemberId is required.");
    }
    @Test
    void createOrderPaymentSucceededNotification_savesNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderPaymentSucceededNotification(orderId, buyerMemberId, 30000L, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(buyerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_PAYMENT_SUCCEEDED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isEqualTo("Payment completed");
        assertThat(saved.getContent()).isEqualTo("Your payment was completed successfully. Amount: 30000");
        assertThat(saved.getCreatedAt()).isEqualTo(occurredAt);
    }

    @Test
    void createOrderPaymentFailedNotification_savesNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderPaymentFailedNotification(
                orderId,
                buyerMemberId,
                OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_PAYMENT_FAILED);
        assertThat(saved.getTitle()).isEqualTo("Payment failed");
        assertThat(saved.getContent()).isEqualTo("Payment failed due to insufficient balance.");
    }
}
