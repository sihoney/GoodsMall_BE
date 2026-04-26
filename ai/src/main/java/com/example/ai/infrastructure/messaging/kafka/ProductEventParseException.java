package com.example.ai.infrastructure.messaging.kafka;

public class ProductEventParseException extends RuntimeException {

    public ProductEventParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
