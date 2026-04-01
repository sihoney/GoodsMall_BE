package com.example.order.presentation.dto.response;

import java.util.List;

public record DeliveryTrackingResponse(
        String courierCode,
        String invoiceNumber,
        boolean delivered,
        List<DeliveryTrackingDetailResponse> details
) {
}
