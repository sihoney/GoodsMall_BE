package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderConfirmedEvent(
        BigDecimal totalPrice,
        UUID auctionId
) {
    public static EventEnvelope<OrderConfirmedEvent> envelopeOf(Order order) {
        OrderConfirmedEvent payload = new OrderConfirmedEvent(order.getTotalPrice(), order.getAuctionId());
        return new EventEnvelope<>(
                UUID.randomUUID(),
                OrderEventType.ORDER_CONFIRMED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                null,
                payload
        );
    }
}
