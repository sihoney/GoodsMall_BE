package com.example.member.verification.application.event;

import java.util.UUID;

public record AccountVerificationFailedEvent(
        UUID memberId,
        String sessionId,
        String reason
) {
}
