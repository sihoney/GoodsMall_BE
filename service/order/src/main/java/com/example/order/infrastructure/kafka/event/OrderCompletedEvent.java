package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.time.Instant;
import java.util.UUID;

public record OrderCompletedEvent(
        UUID orderId,
        UUID sellerMemberId,
        Instant confirmedAt,
        String confirmationType
) {
    public static EventEnvelope<OrderCompletedEvent> envelopeOf(Order order, UUID sellerMemberId) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        OrderCompletedEvent payload = new OrderCompletedEvent(
                order.getOrderId(),
                sellerMemberId,
                now,
                "MANUAL"
        );
        return new EventEnvelope<>(
                eventId,
                OrderEventType.ORDER_PURCHASE_CONFIRMED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                now,
                eventId.toString(),
                payload
        );
    }
}
