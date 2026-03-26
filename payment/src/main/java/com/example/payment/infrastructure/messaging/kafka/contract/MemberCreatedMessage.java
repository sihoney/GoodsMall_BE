package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberCreatedMessage(
        String eventId,
        UUID memberId,
        LocalDateTime occurredAt
) {
}
