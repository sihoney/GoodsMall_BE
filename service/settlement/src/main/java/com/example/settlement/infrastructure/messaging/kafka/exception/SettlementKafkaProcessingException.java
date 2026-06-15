package com.example.settlement.infrastructure.messaging.kafka.exception;

public class SettlementKafkaProcessingException extends RuntimeException {

    public SettlementKafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
