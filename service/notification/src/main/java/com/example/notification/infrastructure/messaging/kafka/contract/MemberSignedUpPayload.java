package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record MemberSignedUpPayload(
        UUID memberId,
        String email
) {
}
