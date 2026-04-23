package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AuctionClosedUnsoldPayload;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AuctionClosedUnsoldNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String EVENT_TYPE = "AUCTION_CLOSED_UNSOLD";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<AuctionClosedUnsoldPayload> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), AuctionClosedUnsoldPayload.class)
        );

        validateAuctionClosedUnsoldEvent(typedEvent);

        notificationUsecase.createAuctionClosedUnsoldNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.aggregateId(),
                typedEvent.recipientId(),
                typedEvent.payload().auctionTitle(),
                toKoreaLocalDateTime(typedEvent.occurredAt())
        );
    }

    private void validateAuctionClosedUnsoldEvent(EventEnvelope<AuctionClosedUnsoldPayload> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("auctionClosedUnsold event is required.");
        }
        if (!EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload is required.");
        }
        if (event.payload().auctionTitle() == null || event.payload().auctionTitle().isBlank()) {
            throw new InvalidEventPayloadException("payload.auctionTitle is required.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
