package com.example.notification.application.mapper;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventMapper {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String MEMBER_SIGNED_UP_EVENT_TYPE = "MEMBER_SIGNED_UP";

    public NotificationCommand toCommand(EventEnvelope<MemberSignedUpPayload> event) {
        validateMemberSignedUp(event);

        UUID memberId = resolveMemberId(event);
        return new NotificationCommand(
                event.eventId(),
                event.traceId(),
                memberId,
                NotificationType.MEMBER_SIGNED_UP,
                "Welcome to TodayLunch",
                "Your account registration is complete.",
                null,
                null,
                toKoreaLocalDateTime(event.occurredAt())
        );
    }

    private void validateMemberSignedUp(EventEnvelope<MemberSignedUpPayload> event) {
        if (event == null) {
            throw new IllegalArgumentException("memberSignedUp event is required.");
        }
        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (!MEMBER_SIGNED_UP_EVENT_TYPE.equals(event.eventType())) {
            throw new IllegalArgumentException("Unsupported eventType: " + event.eventType());
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new IllegalArgumentException("source is required.");
        }
        if (event.recipientId() == null) {
            throw new IllegalArgumentException("recipientId is required.");
        }
        if (event.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
        if (event.payload() == null) {
            throw new IllegalArgumentException("payload is required.");
        }
        if (event.payload().memberId() == null) {
            throw new IllegalArgumentException("payload.memberId is required.");
        }
        if (event.payload().email() == null || event.payload().email().isBlank()) {
            throw new IllegalArgumentException("payload.email is required.");
        }
        if (!event.recipientId().equals(event.payload().memberId())) {
            throw new IllegalArgumentException("recipientId and payload.memberId must match.");
        }
    }

    private UUID resolveMemberId(EventEnvelope<MemberSignedUpPayload> event) {
        return event.recipientId();
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
