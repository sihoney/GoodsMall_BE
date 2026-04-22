package com.example.notification.infrastructure.messaging.kafka.dlq;

public enum NotificationConsumerFailureAction {
    DLQ,
    RETRY,
    IGNORE
}
