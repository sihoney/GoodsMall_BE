package com.example.payment.common.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * Message consumed from the member.signed-up topic.
 * Payload corresponds to the member service MemberSignedUpEvent contract.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberCreatedMessage(
        UUID eventId,
        UUID memberId,
        String email,
        Instant occurredAt
) {
}
