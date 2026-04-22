package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.fasterxml.jackson.databind.JsonNode;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionBidOutbidNotificationEventHandler implements NotificationEventHandler {

    private static final String AUCTION_BID_OUTBID_EVENT_TYPE = "AUCTION_BID_OUTBID";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationUsecase notificationUsecase;

    @Override
    public String supportsEventType() {
        return AUCTION_BID_OUTBID_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        validate(event);

        notificationUsecase.createAuctionBidOutbidNotification(
                event.eventId(),
                event.traceId(),
                event.aggregateId(),
                event.recipientId(),
                LocalDateTime.ofInstant(event.occurredAt(), KOREA_ZONE_ID)
        );
    }

    private void validate(EventEnvelope<JsonNode> event) {
        if (event.eventId() == null) {
            throw new InvalidEventPayloadException("eventId is required.");
        }
        if (!AUCTION_BID_OUTBID_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
        if (event.aggregateId() == null) {
            throw new InvalidEventPayloadException("aggregateId(auctionId) is required.");
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId(bidderId) is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidEventPayloadException("occurredAt is required.");
        }
    }
}
