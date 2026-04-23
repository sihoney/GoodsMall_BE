package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AuctionWonPayload;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AuctionWonNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String EVENT_TYPE = "AUCTION_WON";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<AuctionWonPayload> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), AuctionWonPayload.class)
        );

        validateAuctionWonEvent(typedEvent);

        notificationUsecase.createAuctionWonNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.aggregateId(),
                typedEvent.recipientId(),
                typedEvent.payload().auctionTitle(),
                toLongAmount(typedEvent.payload().finalPrice()),
                toKoreaLocalDateTime(typedEvent.occurredAt())
        );
    }

    private void validateAuctionWonEvent(EventEnvelope<AuctionWonPayload> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("경매 낙찰 이벤트는 필수입니다.");
        }
        if (!EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload는 필수입니다.");
        }
        if (event.payload().auctionTitle() == null || event.payload().auctionTitle().isBlank()) {
            throw new InvalidEventPayloadException("payload.auctionTitle은 필수입니다.");
        }
        if (event.payload().finalPrice() == null) {
            throw new InvalidEventPayloadException("payload.finalPrice는 필수입니다.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }

    private long toLongAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidEventPayloadException("payload.finalPrice는 정수 금액이어야 합니다.", e);
        }
    }
}
