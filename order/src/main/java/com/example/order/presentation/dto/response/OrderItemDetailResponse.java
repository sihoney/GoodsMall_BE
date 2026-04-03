package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.OrderItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDetailResponse(
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        OrderItemStatus status,
        String thumbnailKey
) {
    public static OrderItemDetailResponse from(OrderItem item) {
        return new OrderItemDetailResponse(
                item.getProductId(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getStatus(),
                item.getThumbnailKeySnapshot()
        );
    }
}
