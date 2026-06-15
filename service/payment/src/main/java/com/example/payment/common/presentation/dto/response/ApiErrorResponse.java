package com.example.payment.common.presentation.dto.response;

/**
 * payment API 怨듯넻 ?ㅻ쪟 ?묐떟 DTO??
 */
public record ApiErrorResponse(
        String code,
        String message
) {

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }
}
