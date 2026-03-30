package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
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
                UUID.randomUUID().toString(),
                orderId,
                buyerMemberId,
                UUID.randomUUID(),
                OrderPaymentResultStatus.SUCCESS,
                30000L,
                27000L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                occurredAt
        );

        consumer.listen(event);

        verify(notificationUsecase)
                .createOrderPaymentSucceededNotification(orderId, buyerMemberId, 30000L, occurredAt);
    }

    @Test
    void listen_failureDelegatesToFailedNotification() {
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 29, 9, 10, 2);
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID().toString(),
                orderId,
                buyerMemberId,
                UUID.randomUUID(),
                OrderPaymentResultStatus.FAILED,
                30000L,
                27000L,
                null,
                null,
                OrderPaymentFailureReason.INSUFFICIENT_BALANCE,
                "insufficient balance",
                occurredAt
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
    void listen_throwsWhenFailureReasonMissingOnFailure() {
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderPaymentResultStatus.FAILED,
                30000L,
                27000L,
                null,
                null,
                null,
                "failed",
                LocalDateTime.of(2026, 3, 29, 9, 10, 2)
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("failureReason is required for failure.");
    }
}
