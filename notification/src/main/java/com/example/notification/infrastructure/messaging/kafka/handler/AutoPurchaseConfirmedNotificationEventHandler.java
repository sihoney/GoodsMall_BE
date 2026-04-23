package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.example.notification.infrastructure.messaging.kafka.dlq.exception.InvalidEventPayloadException;
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
            throw new InvalidEventPayloadException("자동 구매 확정 이벤트는 필수입니다.");
        }
        if (!AUTO_PURCHASE_CONFIRMED_EVENT_TYPE.equals(event.eventType())) {
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
        if (event.payload().confirmedAt() == null) {
            throw new InvalidEventPayloadException("payload.confirmedAt은 필수입니다.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().buyerMemberId())) {
            throw new InvalidEventPayloadException("recipientId와 payload.buyerMemberId가 일치해야 합니다.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
