package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderReturnRequestedEvent(
        UUID eventId,
        String eventType,
        UUID orderId,
        UUID buyerId,
        Instant eventCreatedAt,
        List<ReturnOrderLine> returnLines
) {
    public record ReturnOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }

    public static OrderReturnRequestedEvent of(Order order, List<OrderItem> returnItems) {
        return new OrderReturnRequestedEvent(
                UUID.randomUUID(),
                "ORDER_RETURN_REQUESTED",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                returnItems.stream()
                        .map(item -> new ReturnOrderLine(
                                item.getOrderItemId(),
                                item.getProductId(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
    }
}