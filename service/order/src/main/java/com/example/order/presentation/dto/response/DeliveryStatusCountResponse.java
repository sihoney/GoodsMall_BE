package com.example.order.presentation.dto.response;

public record DeliveryStatusCountResponse(
        long preparing,
        long shipped,
        long delivered
) {}
