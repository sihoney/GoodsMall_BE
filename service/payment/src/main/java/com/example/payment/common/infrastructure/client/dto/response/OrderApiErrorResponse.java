package com.example.payment.common.infrastructure.client.dto.response;

public record OrderApiErrorResponse(
        String code,
        String message
) {
}
