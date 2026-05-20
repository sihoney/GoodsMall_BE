package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record AccountVerificationExpiredPayload(
        UUID memberId,
        String sessionId,
        String reason
) {
}
