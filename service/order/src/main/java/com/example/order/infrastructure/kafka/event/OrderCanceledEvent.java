package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCanceledEvent(
        UUID orderId,
        UUID buyerId,
        Instant canceledAt,
        List<CanceledOrderLine> canceledLines
) {
    public record CanceledOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }

    public static EventEnvelope<OrderCanceledEvent> envelopeOf(Order order, List<OrderItem> canceledItems, Instant canceledAt) {
        OrderCanceledEvent payload = new OrderCanceledEvent(
                order.getOrderId(),
                order.getBuyerId(),
                canceledAt,
                canceledItems.stream()
                        .map(item -> new CanceledOrderLine(
                                item.getOrderItemId(),
                                item.getProductId(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        return new EventEnvelope<>(
                UUID.randomUUID(),
                OrderEventType.ORDER_CANCELED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                null,
                payload
        );
    }
}
