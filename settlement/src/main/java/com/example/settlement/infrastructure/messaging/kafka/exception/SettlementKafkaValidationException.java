package com.example.settlement.infrastructure.messaging.kafka.exception;

public class SettlementKafkaValidationException extends RuntimeException {

    public SettlementKafkaValidationException(String message) {
        super(message);
    }

    public SettlementKafkaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
