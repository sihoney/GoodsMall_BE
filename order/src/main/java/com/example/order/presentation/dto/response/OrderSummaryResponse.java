package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID orderId,
        String orderNumber,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt,
        String representativeProductName,
        String representativeThumbnailKey,
        Integer itemCount
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getRepresentativeProductName(),
                order.getRepresentativeThumbnailKey(),
                order.getItemCount());
    }
}
