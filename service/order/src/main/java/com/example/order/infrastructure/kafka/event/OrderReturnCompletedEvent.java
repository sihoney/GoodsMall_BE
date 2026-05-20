package com.example.order.infrastructure.kafka.event;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.OrderEventType;
import com.todaylunch.common.event.contract.EventEnvelope;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderReturnCompletedEvent(
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        UUID returnRequestId,
        UUID orderItemId,
        BigDecimal refundedAmount,
        Instant completedAt
) {
    public static EventEnvelope<OrderReturnCompletedEvent> envelopeOf(
            Order order,
            ReturnRequest returnRequest,
            OrderItem orderItem,
            BigDecimal refundedAmount
    ) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        OrderReturnCompletedEvent payload = new OrderReturnCompletedEvent(
                order.getOrderId(),
                order.getBuyerId(),
                orderItem.getSellerId(),
                returnRequest.getReturnRequestId(),
                orderItem.getOrderItemId(),
                refundedAmount,
                now
        );
        return new EventEnvelope<>(
                eventId,
                OrderEventType.ORDER_RETURN_COMPLETED.name(),
                "order-service",
                order.getOrderId(),
                order.getBuyerId(),
                now,
                eventId.toString(),
                payload
        );
    }
}
