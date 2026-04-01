package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.consumer.OrderPaymentResultEventConsumer;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPaymentResultEventConsumerTest {

    @Mock
    private NotificationUsecase notificationUsecase;

    private OrderPaymentResultEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderPaymentResultEventConsumer(notificationUsecase);
    }

    @Test
    void listen_successDelegatesToSucceededNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID(),
                orderId,
                buyerMemberId,
                BigDecimal.valueOf(30_000L),
                OrderPaymentResultStatus.SUCCESS,
                null,
                Instant.parse("2026-03-29T09:10:02Z")
        );

        consumer.listen(event);

        verify(notificationUsecase)
                .createOrderPaymentSucceededNotification(orderId, buyerMemberId, 30_000L, occurredAt);
    }

    @Test
    void listen_failureDelegatesToFailedNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID(),
                orderId,
                buyerMemberId,
                BigDecimal.valueOf(30_000L),
                OrderPaymentResultStatus.FAILED,
                OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                Instant.parse("2026-03-29T09:10:02Z")
        );

        consumer.listen(event);

        verify(notificationUsecase).createOrderPaymentFailedNotification(
                orderId,
                buyerMemberId,
                OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                occurredAt
        );
    }

    @Test
    void listen_throwsWhenReasonCodeMissingOnFailure() {
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(30_000L),
                OrderPaymentResultStatus.FAILED,
                null,
                Instant.parse("2026-03-29T09:10:02Z")
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reasonCode is required for failure.");
    }
}
