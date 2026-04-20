package com.example.notification.infrastructure.messaging.kafka.dlq;

public class EventParseException extends RuntimeException {

    public EventParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
