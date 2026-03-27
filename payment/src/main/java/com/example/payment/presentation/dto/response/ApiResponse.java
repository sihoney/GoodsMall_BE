package com.example.payment.presentation.dto.response;

/**
 * payment API 공통 성공/실패 응답 래퍼다.
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiErrorResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Object> fail(String code, String message) {
        return new ApiResponse<>(false, null, ApiErrorResponse.of(code, message));
    }
}
