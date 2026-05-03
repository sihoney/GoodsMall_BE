package com.todaylunch.common.event.contract;

import java.time.Instant;
import java.util.UUID;
import tools.jackson.annotation.JsonIgnoreProperties;

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
