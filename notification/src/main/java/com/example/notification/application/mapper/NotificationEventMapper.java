package com.example.notification.application.mapper;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventMapper {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    public NotificationCommand toCommand(EventEnvelope<MemberSignedUpPayload> event) {
        return new NotificationCommand(
                event.eventId(),
                event.traceId(),
                event.recipientId(),
                NotificationType.MEMBER_SIGNED_UP,
                "Welcome to TodayLunch",
                "Your account registration is complete.",
                null,
                null,
                toKoreaLocalDateTime(event.occurredAt())
        );
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
