package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentValidationResponse(
        BigDecimal totalAmount,
        List<OrderLine> items
) {
    public static PaymentValidationResponse from(Order order) {
        return new PaymentValidationResponse(
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(OrderLine::from)
                        .toList()
        );
    }

    record OrderLine(
            UUID orderItemId,
            UUID sellerId,
            BigDecimal lineAmount
    ) {
        static OrderLine from(OrderItem item) {
            return new OrderLine(
                    item.getOrderItemId(),
                    item.getSellerId(),
                    item.getTotalPrice(item.getUnitPriceSnapshot(), item.getQuantity())
            );
        }
    }
}
