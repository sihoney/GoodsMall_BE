package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
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
public class AutoPurchaseConfirmedNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String AUTO_PURCHASE_CONFIRMED_EVENT_TYPE = "AUTO_PURCHASE_CONFIRMED";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return AUTO_PURCHASE_CONFIRMED_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<AutoPurchaseConfirmedMessage> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), AutoPurchaseConfirmedMessage.class)
        );

        validateAutoPurchaseConfirmedEvent(typedEvent);

        notificationUsecase.createAutoPurchaseConfirmedNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.payload().orderId(),
                typedEvent.payload().buyerMemberId(),
                toKoreaLocalDateTime(typedEvent.occurredAt())
        );
    }

    private void validateAutoPurchaseConfirmedEvent(EventEnvelope<AutoPurchaseConfirmedMessage> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("autoPurchaseConfirmed event is required.");
        }
        if (!AUTO_PURCHASE_CONFIRMED_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
        if (event.eventId() == null) {
            throw new InvalidEventPayloadException("eventId is required.");
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidEventPayloadException("source is required.");
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidEventPayloadException("occurredAt is required.");
        }
        if (event.traceId() == null || event.traceId().isBlank()) {
            throw new InvalidEventPayloadException("traceId is required.");
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload is required.");
        }
        if (event.payload().orderId() == null) {
            throw new InvalidEventPayloadException("payload.orderId is required.");
        }
        if (event.payload().buyerMemberId() == null) {
            throw new InvalidEventPayloadException("payload.buyerMemberId is required.");
        }
        if (event.payload().confirmedAt() == null) {
            throw new InvalidEventPayloadException("payload.confirmedAt is required.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().buyerMemberId())) {
            throw new InvalidEventPayloadException("recipientId and payload.buyerMemberId must match.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
