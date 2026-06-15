package com.example.payment.common.infrastructure.client.dto.response;

public record OrderApiResponse<T>(
        boolean success,
        T data,
        OrderApiErrorResponse error
) {
}
