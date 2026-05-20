package com.example.cartservice.wish.presentation.exception.dto;

import org.springframework.validation.FieldError;

public record ValidationError(
    String field,
    Object rejectedValue,
    String message
) {

    public static ValidationError from(FieldError fieldError) {
        return new ValidationError(
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage()
        );
    }
}
