package com.example.product.infrastructure.messaging.kafka;

public final class ProductEventTypes {

    public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
    public static final String PRODUCT_UPDATED = "PRODUCT_UPDATED";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";

    private ProductEventTypes() {}
}
