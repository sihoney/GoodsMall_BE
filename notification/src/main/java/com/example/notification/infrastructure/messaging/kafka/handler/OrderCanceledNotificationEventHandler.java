package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderCanceledMessage;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderCanceledNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String ORDER_CANCELED_EVENT_TYPE = "ORDER_CANCELED";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return ORDER_CANCELED_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<OrderCanceledMessage> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), OrderCanceledMessage.class)
        );

        validateOrderCanceledEvent(typedEvent);

        OrderCanceledMessage payload = typedEvent.payload();
        notificationUsecase.createOrderCanceledNotifications(
                typedEvent.eventId(),
                typedEvent.traceId(),
                payload.orderId(),
                payload.buyerId(),
                payload.canceledLines().stream()
                        .map(OrderCanceledMessage.CanceledOrderLine::sellerId)
                        .filter(Objects::nonNull)
                        .toList(),
                toKoreaLocalDateTime(resolveOccurredAt(typedEvent, payload))
        );
    }

    private void validateOrderCanceledEvent(EventEnvelope<OrderCanceledMessage> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("주문 취소 이벤트는 필수입니다.");
        }
        if (!ORDER_CANCELED_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload는 필수입니다.");
        }
        if (event.payload().eventId() != null && !Objects.equals(event.eventId(), event.payload().eventId())) {
            throw new InvalidEventPayloadException("eventId와 payload.eventId가 일치해야 합니다.");
        }
        if (event.payload().eventType() != null && !ORDER_CANCELED_EVENT_TYPE.equals(event.payload().eventType())) {
            throw new InvalidEventPayloadException("payload.eventType은 ORDER_CANCELED여야 합니다.");
        }
        if (event.payload().orderId() == null) {
            throw new InvalidEventPayloadException("payload.orderId는 필수입니다.");
        }
        if (event.payload().buyerId() == null) {
            throw new InvalidEventPayloadException("payload.buyerId는 필수입니다.");
        }
        if (event.aggregateId() != null && !Objects.equals(event.aggregateId(), event.payload().orderId())) {
            throw new InvalidEventPayloadException("aggregateId와 payload.orderId가 일치해야 합니다.");
        }
        if (event.payload().canceledLines() == null || event.payload().canceledLines().isEmpty()) {
            throw new InvalidEventPayloadException("payload.canceledLines는 필수입니다.");
        }
        boolean hasMissingSeller = event.payload().canceledLines().stream()
                .anyMatch(line -> line == null || line.sellerId() == null);
        if (hasMissingSeller) {
            throw new InvalidEventPayloadException("payload.canceledLines[].sellerId는 필수입니다.");
        }
    }

    private Instant resolveOccurredAt(EventEnvelope<OrderCanceledMessage> event, OrderCanceledMessage payload) {
        if (payload.canceledAt() != null) {
            return payload.canceledAt();
        }
        return event.occurredAt();
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
