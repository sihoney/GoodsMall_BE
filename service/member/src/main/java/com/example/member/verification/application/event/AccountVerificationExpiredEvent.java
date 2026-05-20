package com.example.member.verification.application.event;

import java.util.UUID;

public record AccountVerificationExpiredEvent(
        UUID memberId,
        String sessionId,
        String reason
) {
}
