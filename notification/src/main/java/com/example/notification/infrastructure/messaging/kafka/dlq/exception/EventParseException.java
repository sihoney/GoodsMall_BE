package com.example.notification.infrastructure.messaging.kafka.dlq.exception;

public class EventParseException extends RuntimeException {

    public EventParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
