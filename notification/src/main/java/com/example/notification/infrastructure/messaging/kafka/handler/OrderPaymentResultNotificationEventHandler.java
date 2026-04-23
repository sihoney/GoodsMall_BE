package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.example.notification.infrastructure.messaging.kafka.dlq.exception.InvalidEventPayloadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPaymentResultNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String ORDER_PAYMENT_RESULT_EVENT_TYPE = "ORDER_PAYMENT_RESULT";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return ORDER_PAYMENT_RESULT_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<OrderPaymentResultMessage> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), OrderPaymentResultMessage.class)
        );

        validateOrderPaymentResultEvent(typedEvent);

        LocalDateTime occurredAt = toKoreaLocalDateTime(typedEvent.occurredAt());
        OrderPaymentResultMessage payload = typedEvent.payload();
        if (payload.status() == OrderPaymentResultStatus.SUCCESS) {
            notificationUsecase.createOrderPaymentSucceededNotification(
                    typedEvent.eventId(),
                    typedEvent.traceId(),
                    payload.orderId(),
                    payload.buyerMemberId(),
                    toLongAmount(payload.amount()),
                    occurredAt
            );
            return;
        }

        notificationUsecase.createOrderPaymentFailedNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                payload.orderId(),
                payload.buyerMemberId(),
                mapFailureReason(payload.reasonCode()),
                occurredAt
        );
    }

    private void validateOrderPaymentResultEvent(EventEnvelope<OrderPaymentResultMessage> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("주문 결제 결과 이벤트는 필수입니다.");
        }
        if (!ORDER_PAYMENT_RESULT_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId는 필수입니다.");
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload는 필수입니다.");
        }
        if (event.payload().orderId() == null) {
            throw new InvalidEventPayloadException("payload.orderId는 필수입니다.");
        }
        if (event.payload().buyerMemberId() == null) {
            throw new InvalidEventPayloadException("payload.buyerMemberId는 필수입니다.");
        }
        if (event.payload().amount() == null) {
            throw new InvalidEventPayloadException("payload.amount는 필수입니다.");
        }
        if (event.payload().status() == null) {
            throw new InvalidEventPayloadException("payload.status는 필수입니다.");
        }
        if (event.payload().status() == OrderPaymentResultStatus.FAILED && event.payload().reasonCode() == null) {
            throw new InvalidEventPayloadException("결제 실패 이벤트에는 payload.reasonCode가 필수입니다.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().buyerMemberId())) {
            throw new InvalidEventPayloadException("recipientId와 payload.buyerMemberId가 일치해야 합니다.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }

    private long toLongAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidEventPayloadException("payload.amount는 정수 금액이어야 합니다.", e);
        }
    }

    private OrderPaymentFailureReason mapFailureReason(OrderPaymentFailureReason failureReason) {
        return switch (failureReason) {
            case DUPLICATE_ORDER_PAYMENT -> OrderPaymentFailureReason.DUPLICATE_ORDER_PAYMENT;
            case WALLET_NOT_FOUND -> OrderPaymentFailureReason.WALLET_NOT_FOUND;
            case INSUFFICIENT_BALANCE -> OrderPaymentFailureReason.INSUFFICIENT_BALANCE;
            case INVALID_REQUEST -> OrderPaymentFailureReason.INVALID_REQUEST;
            case INTERNAL_ERROR -> OrderPaymentFailureReason.INTERNAL_ERROR;
        };
    }
}
