package com.example.cartservice.wish.presentation.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    Integer status,
    String error,
    String message,
    String errorCode,
    String path,
    LocalDateTime timestamp,
    List<ValidationError> errors
) {

    public static ErrorResponse of(
        com.example.cartservice.wish.presentation.exception.ErrorCode errorCode,
        String path
    ) {
        return new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getHttpStatus().getReasonPhrase(),
            errorCode.getMessage(),
            errorCode.name(),
            path,
            LocalDateTime.now(),
            null
        );
    }

    public static ErrorResponse of(
        com.example.cartservice.wish.presentation.exception.ErrorCode errorCode,
        String path,
        List<ValidationError> errors
    ) {
        return new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getHttpStatus().getReasonPhrase(),
            errorCode.getMessage(),
            errorCode.name(),
            path,
            LocalDateTime.now(),
            errors
        );
    }
}
