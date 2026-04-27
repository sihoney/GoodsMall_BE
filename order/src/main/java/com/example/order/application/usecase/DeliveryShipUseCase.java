package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.DeliveryShipRequest;
import com.example.order.presentation.dto.response.DeliveryShipResponse;

import java.util.UUID;

public interface DeliveryShipUseCase {
    DeliveryShipResponse startShip(UUID deliveryId, UUID sellerId, DeliveryShipRequest request);
}
