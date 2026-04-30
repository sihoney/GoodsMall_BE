package com.example.ai.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String PRODUCT_CREATED = "product.created";
    public static final String PRODUCT_UPDATED = "product.updated";
    public static final String PRODUCT_DELETED = "product.deleted";
    public static final String PRODUCT_EVENT_DLQ = "ai.product-event.dlq";
    public static final String CART_ITEM_ADDED = "cart.item.added";

    private KafkaTopics() {
    }
}
