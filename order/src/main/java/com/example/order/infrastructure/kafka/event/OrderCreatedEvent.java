package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant orderCreatedAt,
        List<OrderLine> orderLines
) {
    public record OrderLine(
            UUID orderItemId,
            UUID sellerId,
            BigDecimal unitPriceSnapshot,
            int quantity,
            BigDecimal lineTotalPrice
    ) {
    }

    public static EventEnvelope<OrderCreatedEvent> envelopeOf(Order order) {
        OrderCreatedEvent payload = new OrderCreatedEvent(
                order.getOrderId(),
                order.getBuyerId(),
                order.getTotalPrice(),
                order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                order.getItems().stream()
                        .map(item -> new OrderLine(
                                item.getOrderItemId(),
                                item.getSellerId(),
                                item.getUnitPriceSnapshot(),
                                item.getQuantity(),
                                item.getTotalPrice(item.getUnitPriceSnapshot(), item.getQuantity())
                        ))
                        .toList()
        );
        return new EventEnvelope<>(
                UUID.randomUUID(),
                OrderEventType.ORDER_CREATED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                null,
                payload
        );
    }
}
