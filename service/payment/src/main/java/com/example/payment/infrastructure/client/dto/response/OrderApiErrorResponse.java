package com.example.payment.infrastructure.client.dto.response;

public record OrderApiErrorResponse(
        String code,
        String message
) {
}
