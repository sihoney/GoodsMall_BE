package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberOauthLinkedPayload;
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
public class MemberOauthLinkedNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String EVENT_TYPE = "MEMBER_OAUTH_LINKED";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<MemberOauthLinkedPayload> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), MemberOauthLinkedPayload.class)
        );

        validate(typedEvent);
        notificationUsecase.createMemberOauthLinkedNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                typedEvent.recipientId(),
                typedEvent.payload().provider(),
                LocalDateTime.ofInstant(typedEvent.occurredAt(), KOREA_ZONE_ID)
        );
    }

    private void validate(EventEnvelope<MemberOauthLinkedPayload> event) {
        if (event == null || event.payload() == null) {
            throw new InvalidEventPayloadException("소셜 계정 연동 payload는 필수입니다.");
        }
        if (!EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("지원하지 않는 eventType입니다: " + event.eventType());
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId는 필수입니다.");
        }
        if (event.payload().memberId() == null || event.payload().provider() == null || event.payload().provider().isBlank()) {
            throw new InvalidEventPayloadException("payload.memberId와 payload.provider는 필수입니다.");
        }
        if (event.payload().providerUserId() == null || event.payload().providerUserId().isBlank()) {
            throw new InvalidEventPayloadException("payload.providerUserId는 필수입니다.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidEventPayloadException("recipientId와 payload.memberId가 일치해야 합니다.");
        }
    }
}
