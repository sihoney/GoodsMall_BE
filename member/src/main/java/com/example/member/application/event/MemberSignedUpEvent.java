package com.example.member.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberSignedUpEvent(
        UUID eventId,
        UUID memberId,
        String email,
        LocalDateTime occurredAt
) {
}
