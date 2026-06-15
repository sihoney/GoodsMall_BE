package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderReturnRequestedEvent(
        UUID orderId,
        UUID buyerId,
        List<ReturnOrderLine> returnLines
) {
    public record ReturnOrderLine(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }

    public static EventEnvelope<OrderReturnRequestedEvent> envelopeOf(Order order, List<OrderItem> returnItems) {
        OrderReturnRequestedEvent payload = new OrderReturnRequestedEvent(
                order.getOrderId(),
                order.getBuyerId(),
                returnItems.stream()
                        .map(item -> new ReturnOrderLine(
                                item.getOrderItemId(),
                                item.getProductId(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        return new EventEnvelope<>(
                UUID.randomUUID(),
                OrderEventType.ORDER_RETURN_REQUESTED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                null,
                payload
        );
    }
}
