package com.example.order.application.port.dto.response;

import com.example.order.domain.enumtype.ProductOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductInfo(
        UUID productId,
        UUID sellerId,
        String name,
        BigDecimal price,
        String thumbnailKeySnapshot,
        ProductOrderStatus productOrderStatus
) {
}
