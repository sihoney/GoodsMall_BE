package com.example.notification.infrastructure.messaging.kafka.dlq.model;

public enum NotificationDlqReason {
    EVENT_PARSE_FAILURE,
    UNSUPPORTED_EVENT_TYPE,
    INVALID_EVENT_PAYLOAD,
    TEMPORARY_PROCESSING_ERROR,
    IGNORE_DUPLICATE_EVENT
}
