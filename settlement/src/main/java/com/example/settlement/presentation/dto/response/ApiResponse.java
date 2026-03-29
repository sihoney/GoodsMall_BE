package com.example.settlement.presentation.dto.response;

import com.example.settlement.common.exception.ErrorCode;

/**
 * settlement API 공통 성공/실패 응답 래퍼다.
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiErrorResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 표준 ErrorCode 기반 실패 응답을 생성한다.
     */
    public static ApiResponse<Object> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ApiErrorResponse.of(errorCode.name(), errorCode.getMessage()));
    }

    public static ApiResponse<Object> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, ApiErrorResponse.of(errorCode.name(), message));
    }

    public static ApiResponse<Object> fail(String code, String message) {
        return new ApiResponse<>(false, null, ApiErrorResponse.of(code, message));
    }
}
