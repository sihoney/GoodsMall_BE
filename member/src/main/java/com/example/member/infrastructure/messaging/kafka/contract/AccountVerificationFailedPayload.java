package com.example.member.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record AccountVerificationFailedPayload(
        UUID memberId,
        String sessionId,
        String reason
) {
}
