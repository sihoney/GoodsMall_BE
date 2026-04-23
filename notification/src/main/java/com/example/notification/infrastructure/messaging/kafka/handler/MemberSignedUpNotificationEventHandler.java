package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberSignedUpNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String MEMBER_SIGNED_UP_EVENT_TYPE = "MEMBER_SIGNED_UP";

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

        notificationUsecase.createMemberSignedUpNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.recipientId(),
                LocalDateTime.ofInstant(typedEvent.occurredAt(), KOREA_ZONE_ID)
        );
    }

    private void validateMemberSignedUpEvent(EventEnvelope<MemberSignedUpPayload> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("회원가입 이벤트는 필수입니다.");
        }
        if (!MEMBER_SIGNED_UP_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId는 필수입니다.");
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload는 필수입니다.");
        }
        if (event.payload().memberId() == null) {
            throw new InvalidEventPayloadException("payload.memberId는 필수입니다.");
        }
        if (event.payload().email() == null || event.payload().email().isBlank()) {
            throw new InvalidEventPayloadException("payload.email은 필수입니다.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidEventPayloadException("recipientId와 payload.memberId가 일치해야 합니다.");
        }
    }
}
