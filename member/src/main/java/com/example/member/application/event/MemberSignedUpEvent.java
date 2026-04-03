package com.example.member.application.event;

import java.time.Instant;
import java.util.UUID;

public record MemberSignedUpEvent(
        UUID eventId,
        UUID memberId,
        String email,
        Instant occurredAt
) {
}
