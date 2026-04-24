package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        UUID buyerId,
        List<FailedOrderLine> failedLines
) {
    public record FailedOrderLine(
            UUID productId,
            UUID sellerId,
            int quantity
    ) {
    }

    public static EventEnvelope<PaymentFailedEvent> envelopeOf(Order order) {
        PaymentFailedEvent payload = new PaymentFailedEvent(
                order.getOrderId(),
                order.getBuyerId(),
                order.getItems().stream()
                        .map(item -> new FailedOrderLine(
                                item.getProductId(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        return new EventEnvelope<>(
                UUID.randomUUID(),
                OrderEventType.ORDER_FAILED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                Instant.now(),
                null,
                payload
        );
    }
}
