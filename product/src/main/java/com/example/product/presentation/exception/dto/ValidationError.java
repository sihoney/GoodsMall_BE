package com.example.product.presentation.exception.dto;

import org.springframework.validation.FieldError;

/**
 * @Valid 어노테이션 사용 시 발생하는 에러(MethodArgumentNotValidException)
 * 발생시 나오는 에러들의 내용을 담기 위한 RecordClass
 *
 */
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
