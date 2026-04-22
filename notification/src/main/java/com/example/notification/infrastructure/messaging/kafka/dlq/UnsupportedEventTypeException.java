package com.example.notification.infrastructure.messaging.kafka.dlq;

public class UnsupportedEventTypeException extends IllegalArgumentException {

    public UnsupportedEventTypeException(String eventType) {
        super("Unsupported eventType: " + eventType);
    }
}
