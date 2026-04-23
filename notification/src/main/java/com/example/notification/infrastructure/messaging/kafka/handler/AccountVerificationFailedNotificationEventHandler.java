package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AccountVerificationFailedPayload;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AccountVerificationFailedNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String EVENT_TYPE = "ACCOUNT_VERIFICATION_FAILED";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<AccountVerificationFailedPayload> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), AccountVerificationFailedPayload.class)
        );

        validate(typedEvent);
        notificationUsecase.createAccountVerificationFailedNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.recipientId(),
                LocalDateTime.ofInstant(typedEvent.occurredAt(), KOREA_ZONE_ID)
        );
    }

    private void validate(EventEnvelope<AccountVerificationFailedPayload> event) {
        if (event == null || event.payload() == null) {
            throw new InvalidEventPayloadException("accountVerificationFailed payload is required.");
        }
        if (!EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId is required.");
        }
        if (event.payload().memberId() == null || event.payload().sessionId() == null || event.payload().sessionId().isBlank()) {
            throw new InvalidEventPayloadException("payload.memberId and payload.sessionId are required.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidEventPayloadException("recipientId and payload.memberId must match.");
        }
    }
}
