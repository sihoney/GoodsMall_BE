package com.example.notification.infrastructure.messaging.kafka.contract;

public enum PayoutFailureReason {
    WALLET_NOT_FOUND,
    INVALID_PAYOUT_AMOUNT,
    DUPLICATE_PAYOUT,
    SETTLEMENT_NOT_FOUND,
    TEMPORARY_DB_ERROR,
    KAFKA_PUBLISH_ERROR,
    INTERNAL_ERROR
}
