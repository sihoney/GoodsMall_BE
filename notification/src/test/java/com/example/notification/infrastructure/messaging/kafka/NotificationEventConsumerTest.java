package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.consumer.NotificationEventConsumer;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderCreatedMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderCanceledMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerExceptionClassifier;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerFailureDecision;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationDlqReason;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationDlqPublisher;
import com.example.notification.infrastructure.messaging.kafka.handler.AutoPurchaseConfirmedNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.MemberSignedUpNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.NotificationEventHandlerRegistry;
import com.example.notification.infrastructure.messaging.kafka.handler.OrderCreatedNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.OrderCanceledNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.OrderPaymentResultNotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.SellerSettlementPayoutResultNotificationEventHandler;
import com.todaylunch.common.event.contract.EventEnvelope;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
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
        // Matches the Jackson 3 ObjectMapper used by the notification runtime configuration.
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        NotificationEventHandlerRegistry registry = new NotificationEventHandlerRegistry(List.of(
                new MemberSignedUpNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                ),
                new OrderCreatedNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                ),
                new OrderCanceledNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                ),
                new AutoPurchaseConfirmedNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                ),
                new OrderPaymentResultNotificationEventHandler(
                        notificationUsecase,
                        objectMapper
                ),
                new SellerSettlementPayoutResultNotificationEventHandler(
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

        verify(notificationUsecase).createMemberSignedUpNotification(
                eventId,
                "mock-trace-id",
                memberId,
                LocalDateTime.of(2026, 3, 29, 18, 49, 58)
        );
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
    void listen_dispatchesOrderCreatedEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-22T02:10:00Z");
        EventEnvelope<OrderCreatedMessage> envelope = new EventEnvelope<>(
                eventId,
                "ORDER_CREATED",
                "order-service",
                orderId,
                null,
                occurredAt,
                "mock-trace-id",
                new OrderCreatedMessage(
                        eventId,
                        "ORDER_CREATED",
                        orderId,
                        buyerMemberId,
                        BigDecimal.valueOf(25_000L),
                        occurredAt,
                        occurredAt,
                        List.of(
                                new OrderCreatedMessage.OrderLine(
                                        UUID.randomUUID(),
                                        sellerA,
                                        BigDecimal.valueOf(10_000L),
                                        1,
                                        BigDecimal.valueOf(10_000L)
                                ),
                                new OrderCreatedMessage.OrderLine(
                                        UUID.randomUUID(),
                                        sellerB,
                                        BigDecimal.valueOf(15_000L),
                                        1,
                                        BigDecimal.valueOf(15_000L)
                                )
                        )
                )
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listen(message);

        verify(notificationUsecase).createOrderCreatedNotifications(
                eventId,
                "mock-trace-id",
                orderId,
                buyerMemberId,
                25_000L,
                List.of(sellerA, sellerB),
                LocalDateTime.of(2026, 4, 22, 11, 10, 0)
        );
    }

    @Test
    void listen_dispatchesOrderCanceledEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-24T01:05:00Z");
        EventEnvelope<OrderCanceledMessage> envelope = new EventEnvelope<>(
                eventId,
                "ORDER_CANCELED",
                "order-service",
                orderId,
                null,
                occurredAt,
                "mock-trace-id",
                new OrderCanceledMessage(
                        eventId,
                        "ORDER_CANCELED",
                        orderId,
                        buyerMemberId,
                        occurredAt,
                        occurredAt,
                        List.of(
                                new OrderCanceledMessage.CanceledOrderLine(
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        sellerA,
                                        1
                                ),
                                new OrderCanceledMessage.CanceledOrderLine(
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        sellerB,
                                        2
                                )
                        )
                )
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listen(message);

        verify(notificationUsecase).createOrderCanceledNotifications(
                eventId,
                "mock-trace-id",
                orderId,
                buyerMemberId,
                List.of(sellerA, sellerB),
                LocalDateTime.of(2026, 4, 24, 10, 5, 0)
        );
    }

    @Test
    void listen_dispatchesAutoPurchaseConfirmedEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-23T03:15:00Z");
        EventEnvelope<AutoPurchaseConfirmedMessage> envelope = new EventEnvelope<>(
                eventId,
                "AUTO_PURCHASE_CONFIRMED",
                "payment-service",
                orderId,
                buyerMemberId,
                occurredAt,
                "mock-trace-id",
                new AutoPurchaseConfirmedMessage(
                        orderId,
                        buyerMemberId,
                        occurredAt
                )
        );
        String message = objectMapper.writeValueAsString(envelope);

        consumer.listen(message);

        verify(notificationUsecase).createAutoPurchaseConfirmedNotification(
                eventId,
                "mock-trace-id",
                orderId,
                buyerMemberId,
                LocalDateTime.of(2026, 4, 23, 12, 15, 0)
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
    void listenSellerSettlementPayoutResult_dispatchesSuccessEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-22T00:30:00Z");
        EventEnvelope<SellerSettlementPayoutResultMessage> envelope = new EventEnvelope<>(
                eventId,
                "SELLER_SETTLEMENT_PAYOUT_RESULT",
                "payment-service",
                settlementId,
                sellerMemberId,
                occurredAt,
                "mock-trace-id",
                new SellerSettlementPayoutResultMessage(
                        eventId,
                        UUID.randomUUID(),
                        settlementId,
                        sellerMemberId,
                        15_000L,
                        SellerSettlementPayoutResultStatus.SUCCESS,
                        null,
                        LocalDateTime.of(2026, 4, 22, 9, 30, 0)
                )
        );
        String payload = objectMapper.writeValueAsString(envelope);

        consumer.listen(payload);

        verify(notificationUsecase).createSellerSettlementPayoutSucceededNotification(
                eventId,
                "mock-trace-id",
                settlementId,
                sellerMemberId,
                15_000L,
                LocalDateTime.of(2026, 4, 22, 9, 30, 0)
        );
    }

    @Test
    void listenSellerSettlementPayoutResult_dispatchesFailureEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-04-22T00:45:00Z");
        EventEnvelope<SellerSettlementPayoutResultMessage> envelope = new EventEnvelope<>(
                eventId,
                "SELLER_SETTLEMENT_PAYOUT_RESULT",
                "payment-service",
                settlementId,
                sellerMemberId,
                occurredAt,
                "mock-trace-id",
                new SellerSettlementPayoutResultMessage(
                        eventId,
                        UUID.randomUUID(),
                        settlementId,
                        sellerMemberId,
                        null,
                        SellerSettlementPayoutResultStatus.FAILED,
                        PayoutFailureReason.WALLET_NOT_FOUND,
                        LocalDateTime.of(2026, 4, 22, 9, 45, 0)
                )
        );
        String payload = objectMapper.writeValueAsString(envelope);

        consumer.listen(payload);

        verify(notificationUsecase).createSellerSettlementPayoutFailedNotification(
                eventId,
                "mock-trace-id",
                settlementId,
                sellerMemberId,
                PayoutFailureReason.WALLET_NOT_FOUND,
                LocalDateTime.of(2026, 4, 22, 9, 45, 0)
        );
    }

    @Test
    void listenSellerSettlementPayoutResult_publishesToDlqWhenValidationFails() throws Exception {
        UUID sellerMemberId = UUID.randomUUID();
        EventEnvelope<SellerSettlementPayoutResultMessage> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "SELLER_SETTLEMENT_PAYOUT_RESULT",
                "payment-service",
                UUID.randomUUID(),
                sellerMemberId,
                Instant.parse("2026-04-22T01:00:00Z"),
                "mock-trace-id",
                new SellerSettlementPayoutResultMessage(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        sellerMemberId,
                        null,
                        SellerSettlementPayoutResultStatus.FAILED,
                        null,
                        LocalDateTime.of(2026, 4, 22, 10, 0, 0)
                )
        );
        String payload = objectMapper.writeValueAsString(envelope);
        NotificationConsumerFailureDecision decision =
                NotificationConsumerFailureDecision.dlq(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
        when(exceptionClassifier.classify(any(IllegalArgumentException.class))).thenReturn(decision);

        consumer.listen(payload);

        verify(notificationDlqPublisher).publish(
                eq("listenNotificationEvent"),
                eq(payload),
                any(IllegalArgumentException.class),
                eq(decision)
        );
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
