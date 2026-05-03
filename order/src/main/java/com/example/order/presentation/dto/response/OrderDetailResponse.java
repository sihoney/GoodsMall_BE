package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderItemStatus;
import com.example.order.domain.enumtype.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID orderId,
        String orderNumber,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        String address,
        String addressDetail,
        String zipCode,
        String receiver,
        String receiverPhone,
        Integer itemCount,
        OrderStatus status,
        boolean hasOngoingReturn,
        List<OrderItemDetailResponse> items
) {

    public static OrderDetailResponse from(
            Order order,
            List<OrderItemDetailResponse> items
    ) {
        boolean hasOngoingReturn = items.stream()
                .anyMatch(item -> item.status() == OrderItemStatus.RETURN_REQUESTED);
        return new OrderDetailResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getZipCode(),
                order.getReceiver(),
                order.getReceiverPhone(),
                order.getItemCount(),
                order.getStatus(),
                hasOngoingReturn,
                items
        );
    }
}
