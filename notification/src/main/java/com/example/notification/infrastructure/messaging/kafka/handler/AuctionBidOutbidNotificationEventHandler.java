package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class AuctionBidOutbidNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String EVENT_TYPE = "AUCTION_BID_OUTBID";

    private final NotificationUsecase notificationUsecase;

    @Override
    public String supportsEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        validateAuctionBidOutbidEvent(event);

        notificationUsecase.createAuctionOutbidNotification(
                event.eventId(),
                event.traceId(),
                event.aggregateId(),
                event.recipientId(),
                toKoreaLocalDateTime(event.occurredAt())
        );
    }

    private void validateAuctionBidOutbidEvent(EventEnvelope<JsonNode> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("auctionBidOutbid event is required.");
        }
        if (!EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
