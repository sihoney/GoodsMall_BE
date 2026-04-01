package com.example.order.infrastructure.client.dto.response;

public record SweetTrackerTrackingDetail(
        String timeString,
        String where,
        String kind
) {
}