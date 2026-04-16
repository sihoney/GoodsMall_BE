package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCanceledEvent(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID buyerId,
        Instant canceledAt,
        Instant eventCreatedAt,
        List<CanceledOrderLine> canceledLines
) {
    public record CanceledOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }

    public static OrderCanceledEvent of(Order order, List<OrderItem> canceledItems, Instant canceledAt) {
        return new OrderCanceledEvent(
                UUID.randomUUID(),
                "ORDER_CANCELED",
                order.getOrderId(),
                order.getBuyerId(),
                canceledAt,
                Instant.now(),
                canceledItems.stream()
                        .map(item -> new CanceledOrderLine(
                                item.getOrderItemId(),
                                item.getProductId(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
    }
}