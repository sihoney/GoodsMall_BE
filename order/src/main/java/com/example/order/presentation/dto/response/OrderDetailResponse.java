package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID orderId,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        String address,
        String addressDetail,
        String zipCode,
        String receiver,
        String receiverPhone,
        Integer itemCount,
        List<OrderItemDetailResponse> items
) {

    public static OrderDetailResponse from(
            Order order,
            List<OrderItemDetailResponse> items
    ) {
        return new OrderDetailResponse(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getZipCode(),
                order.getReceiver(),
                order.getReceiverPhone(),
                order.getItemCount(),
                items
        );
    }
}
