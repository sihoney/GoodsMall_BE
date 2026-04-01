package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.consumer.AutoPurchaseConfirmedEventConsumer;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoPurchaseConfirmedEventConsumerTest {

    @Mock
    private NotificationUsecase notificationUsecase;

    private AutoPurchaseConfirmedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AutoPurchaseConfirmedEventConsumer(notificationUsecase);
    }

    @Test
    void listen_delegatesToNotificationUsecase() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 29, 18, 49, 58);
        AutoPurchaseConfirmedMessage message = new AutoPurchaseConfirmedMessage(
                orderId,
                buyerMemberId,
                Instant.parse("2026-03-29T09:49:58Z")
        );

        consumer.listen(message);

        verify(notificationUsecase).createAutoPurchaseConfirmedNotification(orderId, buyerMemberId, confirmedAt);
    }

    @Test
    void listen_throwsWhenBuyerMemberIdIsNull() {
        AutoPurchaseConfirmedMessage message = new AutoPurchaseConfirmedMessage(
                UUID.randomUUID(),
                null,
                Instant.parse("2026-03-29T09:49:58Z")
        );

        assertThatThrownBy(() -> consumer.listen(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("buyerMemberId is required.");
    }
}
