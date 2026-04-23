package com.example.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationChannel;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
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
    void createAutoPurchaseConfirmedNotification_savesNotification() {
        assertThat(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED.supportsChannel(NotificationChannel.INBOX)).isTrue();
        assertThat(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED.supportsChannel(NotificationChannel.PUSH)).isTrue();

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
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        assertThat(saved.getCreatedAt()).isEqualTo(confirmedAt);
        assertThat(saved.isRead()).isFalse();
        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED);
        verify(notificationPushService).push(any());
    }

    @Test
    void createMemberSignedUpNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 18, 49, 58);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createMemberSignedUpNotification(eventId, "trace-id", memberId, occurredAt);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getMemberId()).isEqualTo(memberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_SIGNUP_COMPLETED);
        assertThat(saved.getTitle()).isEqualTo("회원가입을 환영해요");
        assertThat(saved.getContent()).isEqualTo("투데이런치 회원가입이 완료되었어요.");
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_SIGNUP_COMPLETED);
        verify(notificationPushService).push(any());
    }

    @Test
    void createAutoPurchaseConfirmedNotification_recordsDuplicateMetricWhenEventAlreadyExists() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        when(notificationJpaRepository.existsByEventIdAndMemberIdAndType(
                eventId,
                buyerMemberId,
                NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED
        )).thenReturn(true);

        notificationService.createAutoPurchaseConfirmedNotification(eventId, null, orderId, buyerMemberId, confirmedAt);

        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED);
        verify(notificationMetricsRecorder).recordDuplicateEvent(NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED);
        verify(notificationJpaRepository, never()).save(any(Notification.class));
        verify(notificationPushService, never()).push(any());
    }

    @Test
    void createOrderCreatedNotifications_createsBuyerAndDistinctSellerNotifications() {
        assertThat(NotificationType.BUYER_ORDER_CREATED.supportsChannel(NotificationChannel.INBOX)).isTrue();
        assertThat(NotificationType.BUYER_ORDER_CREATED.supportsChannel(NotificationChannel.PUSH)).isFalse();
        assertThat(NotificationType.SELLER_ORDER_RECEIVED.supportsChannel(NotificationChannel.PUSH)).isTrue();

        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 11, 15, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderCreatedNotifications(
                eventId,
                "trace-id",
                orderId,
                buyerMemberId,
                25_000L,
                List.of(sellerA, sellerA, sellerB),
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository, org.mockito.Mockito.times(3)).save(captor.capture());

        List<Notification> savedNotifications = captor.getAllValues();
        assertThat(savedNotifications).hasSize(3);
        assertThat(savedNotifications)
                .extracting(Notification::getMemberId)
                .containsExactly(buyerMemberId, sellerA, sellerB);
        assertThat(savedNotifications)
                .extracting(Notification::getType)
                .containsExactly(
                        NotificationType.BUYER_ORDER_CREATED,
                        NotificationType.SELLER_ORDER_RECEIVED,
                        NotificationType.SELLER_ORDER_RECEIVED
                );
        assertThat(savedNotifications)
                .extracting(Notification::getReferenceId)
                .containsOnly(orderId);

        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.BUYER_ORDER_CREATED);
        verify(notificationMetricsRecorder, org.mockito.Mockito.times(2))
                .recordEventReceived(NotificationType.SELLER_ORDER_RECEIVED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_ORDER_CREATED);
        verify(notificationMetricsRecorder, org.mockito.Mockito.times(2))
                .recordSaved(NotificationType.SELLER_ORDER_RECEIVED);
        verify(notificationPushService, org.mockito.Mockito.times(2)).push(any());
    }

    @Test
    void createOrderCreatedNotifications_skipsDuplicatePerMemberAndType() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 22, 11, 30, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(notificationJpaRepository.existsByEventIdAndMemberIdAndType(
                eventId,
                buyerMemberId,
                NotificationType.BUYER_ORDER_CREATED
        )).thenReturn(true);

        notificationService.createOrderCreatedNotifications(
                eventId,
                "trace-id",
                orderId,
                buyerMemberId,
                12_000L,
                List.of(sellerMemberId),
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(sellerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_ORDER_RECEIVED);
        verify(notificationMetricsRecorder).recordDuplicateEvent(NotificationType.BUYER_ORDER_CREATED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.SELLER_ORDER_RECEIVED);
        verify(notificationPushService).push(any());
    }

    @Test
    void createOrderCanceledNotifications_createsBuyerAndDistinctSellerNotifications() {
        assertThat(NotificationType.BUYER_ORDER_CANCELED.supportsChannel(NotificationChannel.INBOX)).isTrue();
        assertThat(NotificationType.BUYER_ORDER_CANCELED.supportsChannel(NotificationChannel.PUSH)).isTrue();
        assertThat(NotificationType.SELLER_ORDER_CANCELED.supportsChannel(NotificationChannel.PUSH)).isTrue();

        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 24, 10, 5, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createOrderCanceledNotifications(
                eventId,
                "trace-id",
                orderId,
                buyerMemberId,
                List.of(sellerA, sellerA, sellerB),
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository, org.mockito.Mockito.times(3)).save(captor.capture());

        List<Notification> savedNotifications = captor.getAllValues();
        assertThat(savedNotifications)
                .extracting(Notification::getMemberId)
                .containsExactly(buyerMemberId, sellerA, sellerB);
        assertThat(savedNotifications)
                .extracting(Notification::getType)
                .containsExactly(
                        NotificationType.BUYER_ORDER_CANCELED,
                        NotificationType.SELLER_ORDER_CANCELED,
                        NotificationType.SELLER_ORDER_CANCELED
                );

        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_ORDER_CANCELED);
        verify(notificationMetricsRecorder, org.mockito.Mockito.times(2))
                .recordSaved(NotificationType.SELLER_ORDER_CANCELED);
        verify(notificationPushService, org.mockito.Mockito.times(3)).push(any());
    }

    @Test
    void createAutoPurchaseConfirmedNotification_throwsWhenBuyerMemberIdIsNull() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 9, 49, 58);

        assertThatThrownBy(() ->
                notificationService.createAutoPurchaseConfirmedNotification(eventId, null, orderId, null, confirmedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("memberId는 필수입니다.");
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
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_ORDER_PAYMENT_SUCCEEDED);
        assertThat(saved.getReferenceId()).isEqualTo(orderId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.ORDER);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        assertThat(saved.getCreatedAt()).isEqualTo(occurredAt);
        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.BUYER_ORDER_PAYMENT_SUCCEEDED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_ORDER_PAYMENT_SUCCEEDED);
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
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_ORDER_PAYMENT_FAILED);
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContent()).isNotBlank();
        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.BUYER_ORDER_PAYMENT_FAILED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_ORDER_PAYMENT_FAILED);
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
        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.SELLER_SETTLEMENT_PAYOUT_SUCCEEDED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.SELLER_SETTLEMENT_PAYOUT_SUCCEEDED);
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
        verify(notificationMetricsRecorder).recordEventReceived(NotificationType.SELLER_SETTLEMENT_PAYOUT_FAILED);
        verify(notificationMetricsRecorder).recordSaved(NotificationType.SELLER_SETTLEMENT_PAYOUT_FAILED);
        verify(notificationPushService).push(any());
    }

    @Test
    void createAuctionWonNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID winnerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 23, 12, 0, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAuctionWonNotification(
                eventId,
                "trace-id",
                auctionId,
                winnerMemberId,
                "봄 도시락 세트",
                150_000L,
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(winnerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_AUCTION_WON);
        assertThat(saved.getReferenceId()).isEqualTo(auctionId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.AUCTION);
        assertThat(saved.getContent()).contains("봄 도시락 세트", "150000원");
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_AUCTION_WON);
        verify(notificationPushService).push(any());
    }

    @Test
    void createAuctionOutbidNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID outbidBidderId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 23, 11, 55, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAuctionOutbidNotification(
                eventId,
                "trace-id",
                auctionId,
                outbidBidderId,
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(outbidBidderId);
        assertThat(saved.getType()).isEqualTo(NotificationType.BUYER_AUCTION_OUTBID);
        assertThat(saved.getReferenceId()).isEqualTo(auctionId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.AUCTION);
        assertThat(saved.getContent()).contains("더 높은 금액");
        verify(notificationMetricsRecorder).recordSaved(NotificationType.BUYER_AUCTION_OUTBID);
        verify(notificationPushService).push(any());
    }

    @Test
    void createAuctionClosedSoldNotification_savesNotification() {
        UUID eventId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 23, 12, 5, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAuctionClosedSoldNotification(
                eventId,
                "trace-id",
                auctionId,
                sellerMemberId,
                "프리미엄 샐러드",
                90_000L,
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(sellerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_AUCTION_CLOSED_SOLD);
        assertThat(saved.getReferenceId()).isEqualTo(auctionId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.AUCTION);
        assertThat(saved.getContent()).contains("프리미엄 샐러드", "90000원");
        verify(notificationMetricsRecorder).recordSaved(NotificationType.SELLER_AUCTION_CLOSED_SOLD);
        verify(notificationPushService).push(any());
    }

    @Test
    void createAuctionClosedUnsoldNotification_savesNotificationWithoutBidCount() {
        UUID eventId = UUID.randomUUID();
        UUID auctionId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 23, 12, 10, 0);

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createAuctionClosedUnsoldNotification(
                eventId,
                "trace-id",
                auctionId,
                sellerMemberId,
                "마감 특가 도시락",
                occurredAt
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationJpaRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(sellerMemberId);
        assertThat(saved.getType()).isEqualTo(NotificationType.SELLER_AUCTION_CLOSED_UNSOLD);
        assertThat(saved.getReferenceId()).isEqualTo(auctionId);
        assertThat(saved.getReferenceType()).isEqualTo(NotificationReferenceType.AUCTION);
        assertThat(saved.getContent()).contains("마감 특가 도시락", "입찰 없이 종료");
        verify(notificationMetricsRecorder).recordSaved(NotificationType.SELLER_AUCTION_CLOSED_UNSOLD);
        verify(notificationPushService).push(any());
    }
}
