package com.example.order.infrastructure.client.dto.response;

import com.example.order.domain.enumtype.ProductOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductAvailabilityResponse(
        UUID productId,
        UUID sellerId,
        String name,
        BigDecimal price,
        String thumbnailKeySnapshot,
        ProductOrderStatus productOrderStatus
) {
}
