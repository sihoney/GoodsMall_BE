package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.notification.application.mapper.NotificationEventMapper;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.consumer.NotificationEventConsumer;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerExceptionClassifier;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerFailureDecision;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationDlqReason;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationDlqPublisher;
import com.example.notification.infrastructure.messaging.kafka.handler.MemberSignedUpNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.NotificationEventHandlerRegistry;
import com.example.notification.infrastructure.messaging.kafka.handler.OrderPaymentResultNotificationEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationUsecase notificationUsecase;

    @Mock
    private NotificationConsumerExceptionClassifier exceptionClassifier;

    @Mock
    private NotificationDlqPublisher notificationDlqPublisher;

    private NotificationEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        NotificationEventHandlerRegistry registry = new NotificationEventHandlerRegistry(List.of(
                new MemberSignedUpNotificationEventHandler(
                        new NotificationEventMapper(),
                        notificationUsecase,
                        objectMapper
                ),
                new OrderPaymentResultNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                )
        ));

        consumer = new NotificationEventConsumer(
                objectMapper,
                registry,
                exceptionClassifier,
                notificationDlqPublisher
        );
    }

    @Test
    void listen_dispatchesMemberSignedUpEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-29T09:49:58Z");
        EventEnvelope<MemberSignedUpPayload> envelope = new EventEnvelope<>(
                eventId,
                "MEMBER_SIGNED_UP",
                "member-service",
                memberId,
                memberId,
                occurredAt,
                "mock-trace-id",
                new MemberSignedUpPayload(memberId, "user@example.com")
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listen(message);

        verify(notificationUsecase).createNotification(any());
        verifyNoInteractions(exceptionClassifier, notificationDlqPublisher);
    }

    @Test
    void listen_dispatchesOrderPaymentResultSuccessEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-29T10:49:58Z");
        EventEnvelope<OrderPaymentResultMessage> envelope = new EventEnvelope<>(
                eventId,
                "ORDER_PAYMENT_RESULT",
                "payment-service",
                orderId,
                buyerMemberId,
                occurredAt,
                "mock-trace-id",
                new OrderPaymentResultMessage(
                        eventId,
                        orderId,
                        buyerMemberId,
                        BigDecimal.valueOf(12_000L),
                        OrderPaymentResultStatus.SUCCESS,
                        null,
                        occurredAt
                )
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listen(message);

        verify(notificationUsecase).createOrderPaymentSucceededNotification(
                eventId,
                "mock-trace-id",
                orderId,
                buyerMemberId,
                12_000L,
                LocalDateTime.of(2026, 3, 29, 19, 49, 58)
        );
    }

    @Test
    void listen_publishesToDlqWhenValidationFails() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID payloadMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-29T09:49:58Z");
        EventEnvelope<MemberSignedUpPayload> envelope = new EventEnvelope<>(
                eventId,
                "MEMBER_SIGNED_UP",
                "member-service",
                recipientId,
                recipientId,
                occurredAt,
                "mock-trace-id",
                new MemberSignedUpPayload(payloadMemberId, "user@example.com")
        );
        String message = objectMapper.writeValueAsString(envelope);
        NotificationConsumerFailureDecision decision =
                NotificationConsumerFailureDecision.dlq(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
        when(exceptionClassifier.classify(any(IllegalArgumentException.class))).thenReturn(decision);

        consumer.listen(message);

        verify(exceptionClassifier).classify(any(IllegalArgumentException.class));
        verify(notificationDlqPublisher).publish(
                eq("listenNotificationEvent"),
                eq(message),
                any(IllegalArgumentException.class),
                eq(decision)
        );
        verifyNoInteractions(notificationUsecase);
    }

    @Test
    void listen_publishesToDlqWhenEventTypeIsUnsupported() throws Exception {
        EventEnvelope<Object> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "UNKNOWN_EVENT_TYPE",
                "unknown-service",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-03-29T10:49:58Z"),
                "mock-trace-id",
                Map.of("raw", "payload")
        );
        String message = objectMapper.writeValueAsString(envelope);
        NotificationConsumerFailureDecision decision =
                NotificationConsumerFailureDecision.dlq(NotificationDlqReason.UNSUPPORTED_EVENT_TYPE);
        when(exceptionClassifier.classify(any(IllegalArgumentException.class))).thenReturn(decision);

        consumer.listen(message);

        verify(notificationDlqPublisher).publish(
                eq("listenNotificationEvent"),
                eq(message),
                any(IllegalArgumentException.class),
                eq(decision)
        );
    }

    @Test
    void listen_rethrowsWhenClassifierReturnsRetry() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-29T10:49:58Z");
        EventEnvelope<OrderPaymentResultMessage> envelope = new EventEnvelope<>(
                eventId,
                "ORDER_PAYMENT_RESULT",
                "payment-service",
                orderId,
                buyerMemberId,
                occurredAt,
                "mock-trace-id",
                new OrderPaymentResultMessage(
                        eventId,
                        orderId,
                        buyerMemberId,
                        null,
                        OrderPaymentResultStatus.SUCCESS,
                        null,
                        occurredAt
                )
        );
        String message = objectMapper.writeValueAsString(envelope);
        when(exceptionClassifier.classify(any(IllegalArgumentException.class)))
                .thenReturn(NotificationConsumerFailureDecision.retry(NotificationDlqReason.TEMPORARY_PROCESSING_ERROR));

        assertThatThrownBy(() -> consumer.listen(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("payload.amount is required.");

        verify(exceptionClassifier).classify(any(IllegalArgumentException.class));
        verifyNoInteractions(notificationDlqPublisher);
    }
}
