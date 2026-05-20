package com.example.payment.common.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record CardConfirmResultMessage(
        UUID eventId,
        UUID orderId,
        CardConfirmResultStatus status,
        @JsonProperty("fail_reason")
        String failReason,
        Instant occurredAt
) {
}
