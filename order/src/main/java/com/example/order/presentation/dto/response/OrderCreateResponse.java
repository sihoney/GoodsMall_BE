package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreateResponse(
        UUID orderId,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt) {

    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getCreatedAt());
    }
}