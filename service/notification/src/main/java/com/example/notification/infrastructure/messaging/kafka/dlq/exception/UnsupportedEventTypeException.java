package com.example.notification.infrastructure.messaging.kafka.dlq.exception;

public class UnsupportedEventTypeException extends IllegalArgumentException {

    public UnsupportedEventTypeException(String eventType) {
        super("지원하지 않는 eventType입니다: " + eventType);
    }
}
