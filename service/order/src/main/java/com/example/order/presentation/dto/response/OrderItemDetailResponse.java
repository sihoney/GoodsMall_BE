package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.OrderItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDetailResponse(
        UUID orderItemId,
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        OrderItemStatus status,
        String thumbnailKey,
        UUID deliveryId
) {
    public static OrderItemDetailResponse from(OrderItem item, UUID deliveryId, String thumbnailUrl) {
        return new OrderItemDetailResponse(
                item.getOrderItemId(),
                item.getProductId(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getStatus(),
                thumbnailUrl,
                deliveryId
        );
    }
}
