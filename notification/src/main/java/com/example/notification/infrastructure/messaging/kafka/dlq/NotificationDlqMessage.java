package com.example.notification.infrastructure.messaging.kafka.dlq;

import java.time.Instant;

public record NotificationDlqMessage(
        String listenerName,
        String reason,
        String exceptionType,
        String exceptionMessage,
        String rawMessage,
        Instant failedAt
) {
}
