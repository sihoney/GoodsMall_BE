package com.example.order.infrastructure.client.dto;

import com.example.order.domain.enumtype.ProductOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID productId,
        UUID sellerId,
        String name,
        BigDecimal price,
        ProductOrderStatus productOrderStatus
) {
}
