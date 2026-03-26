package com.example.payment.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberCreatedEvent(
        String eventId,
        UUID memberId,
        LocalDateTime occurredAt
) {
}
