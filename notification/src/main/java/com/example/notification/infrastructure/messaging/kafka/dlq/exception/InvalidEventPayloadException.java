package com.example.notification.infrastructure.messaging.kafka.dlq.exception;

public class InvalidEventPayloadException extends IllegalArgumentException {

    public InvalidEventPayloadException(String message) {
        super(message);
    }

    public InvalidEventPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
