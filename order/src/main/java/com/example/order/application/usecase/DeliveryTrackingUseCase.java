package com.example.order.application.usecase;

import com.example.order.presentation.dto.response.DeliveryTrackingResponse;

import java.util.UUID;

public interface DeliveryTrackingUseCase {
    DeliveryTrackingResponse getTrackingInfo(UUID deliveryId, UUID memberId);
}
