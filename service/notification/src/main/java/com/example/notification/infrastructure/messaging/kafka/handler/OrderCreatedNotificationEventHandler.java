package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderCreatedMessage;
import com.example.notification.infrastructure.messaging.kafka.dlq.exception.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderCreatedNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String ORDER_CREATED_EVENT_TYPE = "ORDER_CREATED";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return ORDER_CREATED_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<OrderCreatedMessage> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), OrderCreatedMessage.class)
        );

        validateOrderCreatedEvent(typedEvent);

        OrderCreatedMessage payload = typedEvent.payload();
        notificationUsecase.createOrderCreatedNotifications(
                typedEvent.eventId(),
                typedEvent.traceId(),
                payload.orderId(),
                payload.buyerId(),
                toLongAmount(payload.totalPrice()),
                payload.orderLines().stream()
                        .map(OrderCreatedMessage.OrderLine::sellerId)
                        .filter(Objects::nonNull)
                        .toList(),
                toKoreaLocalDateTime(resolveOccurredAt(typedEvent, payload))
        );
    }

    private void validateOrderCreatedEvent(EventEnvelope<OrderCreatedMessage> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("주문 생성 이벤트는 필수입니다.");
        }
        if (!ORDER_CREATED_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload는 필수입니다.");
        }
        if (event.payload().eventId() != null && !Objects.equals(event.eventId(), event.payload().eventId())) {
            throw new InvalidEventPayloadException("eventId와 payload.eventId가 일치해야 합니다.");
        }
        if (event.payload().eventType() != null && !ORDER_CREATED_EVENT_TYPE.equals(event.payload().eventType())) {
            throw new InvalidEventPayloadException("payload.eventType은 ORDER_CREATED여야 합니다.");
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
        if (event.payload().totalPrice() == null) {
            throw new InvalidEventPayloadException("payload.totalPrice는 필수입니다.");
        }
        if (event.payload().orderLines() == null || event.payload().orderLines().isEmpty()) {
            throw new InvalidEventPayloadException("payload.orderLines는 필수입니다.");
        }
        boolean hasMissingSeller = event.payload().orderLines().stream().anyMatch(line -> line == null || line.sellerId() == null);
        if (hasMissingSeller) {
            throw new InvalidEventPayloadException("payload.orderLines[].sellerId는 필수입니다.");
        }
    }

    private Instant resolveOccurredAt(EventEnvelope<OrderCreatedMessage> event, OrderCreatedMessage payload) {
        if (payload.orderCreatedAt() != null) {
            return payload.orderCreatedAt();
        }
        return event.occurredAt();
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }

    private long toLongAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidEventPayloadException("payload.totalPrice는 정수 금액이어야 합니다.", e);
        }
    }
}
