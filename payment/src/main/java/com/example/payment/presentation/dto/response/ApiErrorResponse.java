package com.example.payment.presentation.dto.response;

/**
 * payment API 공통 오류 응답 DTO다.
 */
public record ApiErrorResponse(
        String code,
        String message
) {

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }
}
