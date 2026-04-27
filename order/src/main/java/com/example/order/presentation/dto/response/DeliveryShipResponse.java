package com.example.order.presentation.dto.response;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.enumtype.DeliveryStatus;

public record DeliveryShipResponse(
        String courier,
        String invoiceNumber,
        DeliveryStatus status
) {
    public static DeliveryShipResponse from(Delivery delivery, String courierName) {
        return new DeliveryShipResponse(
                courierName,
                delivery.getInvoiceNumber(),
                delivery.getStatus()
        );
    }
}
