package com.example.member.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record AccountVerificationExpiredPayload(
        UUID memberId,
        String sessionId,
        String reason
) {
}
