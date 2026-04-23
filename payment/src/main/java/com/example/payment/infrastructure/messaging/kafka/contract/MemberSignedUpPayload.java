package com.example.payment.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberSignedUpPayload(
        UUID memberId,
        String email
) {
}
