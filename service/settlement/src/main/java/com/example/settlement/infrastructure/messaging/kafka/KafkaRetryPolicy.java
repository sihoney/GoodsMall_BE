package com.example.settlement.infrastructure.messaging.kafka;

public final class KafkaRetryPolicy {

    public static final long INITIAL_INTERVAL_MS = 1000L;
    public static final double MULTIPLIER = 2.0;
    public static final long MAX_INTERVAL_MS = 10000L;
    public static final int MAX_RETRIES = 3;

    private KafkaRetryPolicy() {
    }
}
