package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.application.mapper.NotificationEventMapper;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberSignedUpNotificationEventHandler implements NotificationEventHandler {

    private static final String MEMBER_SIGNED_UP_EVENT_TYPE = "MEMBER_SIGNED_UP";

    private final NotificationEventMapper notificationEventMapper;
    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return MEMBER_SIGNED_UP_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<MemberSignedUpPayload> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), MemberSignedUpPayload.class)
        );

        validateMemberSignedUpEvent(typedEvent);

        NotificationCommand command = notificationEventMapper.toCommand(typedEvent);
        notificationUsecase.createNotification(command);
    }

    private void validateMemberSignedUpEvent(EventEnvelope<MemberSignedUpPayload> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("memberSignedUp event is required.");
        }
        if (event.eventId() == null) {
            throw new InvalidEventPayloadException("eventId is required.");
        }
        if (!MEMBER_SIGNED_UP_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
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
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload is required.");
        }
        if (event.payload().memberId() == null) {
            throw new InvalidEventPayloadException("payload.memberId is required.");
        }
        if (event.payload().email() == null || event.payload().email().isBlank()) {
            throw new InvalidEventPayloadException("payload.email is required.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidEventPayloadException("recipientId and payload.memberId must match.");
        }
    }
}
