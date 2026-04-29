package com.example.order.infrastructure.kafka;

public final class KafkaTopics {

    public static final String PAYMENT_RESULT = "payment.card-confirm-result";
    public static final String ORDER_CANCELED = "order.canceled";
    public static final String ORDER_RETURN_REQUESTED = "order.return-requested";
    public static final String PAYMENT_FAILED = "order.payment-failed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String AUCTION_WON = "auction.won";

    private KafkaTopics() {
    }
}
