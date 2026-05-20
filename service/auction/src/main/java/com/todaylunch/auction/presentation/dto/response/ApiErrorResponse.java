package com.todaylunch.auction.presentation.dto.response;

public record ApiErrorResponse(
        String code,
        String message
) {

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }
}
