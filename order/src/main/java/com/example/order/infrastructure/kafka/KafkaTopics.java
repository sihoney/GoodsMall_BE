package com.example.order.infrastructure.kafka;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";
    public static final String PAYMENT_RESULT = "payment.order-payment-result";

    private KafkaTopics() {
    }
}
