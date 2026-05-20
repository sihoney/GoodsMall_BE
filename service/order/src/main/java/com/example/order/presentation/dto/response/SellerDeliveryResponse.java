package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SellerDeliveryResponse(
        UUID deliveryId,
        UUID orderId,
        String orderNumber,
        String productName,
        Integer quantity,
        DeliveryStatus status,
        String courierCode,
        String courierName,
        String invoiceNumber,
        String receiver,
        String receiverPhone,
        String address,
        String addressDetail,
        String zipCode,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {
    public static SellerDeliveryResponse from(Delivery delivery, String courierName) {
        OrderItem item = delivery.getOrderItem();
        Order order = item.getOrder();
        return new SellerDeliveryResponse(
                delivery.getDeliveryId(),
                order.getOrderId(),
                order.getOrderNumber(),
                item.getProductNameSnapshot(),
                item.getQuantity(),
                delivery.getStatus(),
                delivery.getCourierCode(),
                courierName,
                delivery.getInvoiceNumber(),
                order.getReceiver(),
                order.getReceiverPhone(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getZipCode(),
                delivery.getShippedAt(),
                delivery.getDeliveredAt(),
                delivery.getCreatedAt()
        );
    }
}
