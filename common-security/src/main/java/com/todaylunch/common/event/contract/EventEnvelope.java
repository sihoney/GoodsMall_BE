package com.todaylunch.common.event.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String source,
        UUID aggregateId,
        UUID recipientId,
        Instant occurredAt,
        String traceId,
        T payload
) {
}
