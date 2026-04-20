package com.example.order.presentation.dto.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiErrorResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Object> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiErrorResponse(code, message));
    }

    public record ApiErrorResponse(
            String code,
            String message
    ) {}
}
