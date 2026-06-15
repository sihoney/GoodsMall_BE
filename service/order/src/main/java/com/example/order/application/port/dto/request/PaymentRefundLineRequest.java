package com.example.order.application.port.dto.request;

import com.example.order.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundLineRequest(
        UUID orderItemId,
        BigDecimal refundAmount
) {
    public static PaymentRefundLineRequest from(OrderItem orderItem) {
        return new PaymentRefundLineRequest(
                orderItem.getOrderItemId(),
                orderItem.getTotalPrice(orderItem.getUnitPriceSnapshot(), orderItem.getQuantity())
        );
    }
}
