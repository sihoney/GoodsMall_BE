package com.example.member.member.infrastructure.client.dto.response;

public record DeliveryStatusCountResponse(
        long preparing,
        long shipped,
        long delivered
) {
}
