package com.example.order.application.port;

import com.example.order.presentation.dto.response.DeliveryTrackingResponse;

public interface TrackingPort {
    DeliveryTrackingResponse getTrackingInfo(String courierCode, String invoiceNumber);
}
