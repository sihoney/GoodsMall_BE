package com.example.ai.infrastructure.messaging.kafka.dlq;

import java.time.Instant;

public record ProductEventDlqMessage(
        String listenerName,
        String topic,
        String reason,
        String exceptionType,
        String exceptionMessage,
        String rawMessage,
        Instant failedAt
) {
}
