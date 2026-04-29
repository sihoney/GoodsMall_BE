package com.example.product.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String PRODUCT_CREATED = "product.created";
    public static final String PRODUCT_UPDATED = "product.updated";
    public static final String PRODUCT_DELETED = "product.deleted";
    public static final String AUCTION_WON     = "auction.won";

    private KafkaTopics() {}
}
