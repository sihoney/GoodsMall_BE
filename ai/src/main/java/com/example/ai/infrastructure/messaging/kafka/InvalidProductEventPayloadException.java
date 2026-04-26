package com.example.ai.infrastructure.messaging.kafka;

public class InvalidProductEventPayloadException extends IllegalArgumentException {

    public InvalidProductEventPayloadException(String message) {
        super(message);
    }

    public InvalidProductEventPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
