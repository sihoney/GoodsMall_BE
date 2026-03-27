package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getCreatedAt());
    }
}