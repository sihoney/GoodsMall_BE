package com.example.order.infrastructure.client.dto.request;

import com.example.order.application.port.dto.request.ProductStockDeductRequest;

import java.util.UUID;

public record ExternalProductRequest(
        UUID productId,
        Integer quantity
) {
    public static ExternalProductRequest from(ProductStockDeductRequest request) {
        return new ExternalProductRequest(request.productId(), request.quantity());
    }
}
