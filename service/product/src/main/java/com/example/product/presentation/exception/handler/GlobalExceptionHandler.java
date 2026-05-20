package com.example.product.presentation.exception.handler;

import com.example.product.common.exception.CustomException;
import com.example.product.common.exception.ErrorCode;
import com.example.product.presentation.exception.dto.ErrorResponse;
import com.example.product.presentation.exception.dto.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
        CustomException ex,
        HttpServletRequest request
    ) {
        log.warn("CustomException: {}", ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, request.getRequestURI());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<ValidationError> errors = makeValidationErrors(ex);

        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI(),
            errors
        );

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI()
        );
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
        IllegalStateException ex,
        HttpServletRequest request
    ) {
        log.warn("IllegalStateException: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI()
        );
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(
        NullPointerException ex,
        HttpServletRequest request
    ) {
        log.warn("NullPointerException: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCode.INVALID_INPUT_VALUE,
            request.getRequestURI()
        );
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
            .body(response);
    }

    private static List<ValidationError> makeValidationErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(ValidationError::from)
            .toList();
    }
}
