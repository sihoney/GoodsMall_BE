package com.example.member.common.presentation.web.exception;

import com.example.member.common.exception.BusinessException;
import com.example.member.common.exception.ErrorCode;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import com.todaylunch.common.security.exception.InvalidTokenException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.fail(errorCode.code(), exception.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDenied(AuthorizationDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("ACCESS_DENIED", exception.getMessage()));
    }

    // TODO: migrate common security token failures to BusinessException + AuthErrorCode when auth error handling is unified.
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }

    // TODO: validation error message extraction is duplicated; extract a private formatter method.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("VALIDATION_ERROR", message));
    }

    // TODO: migrate remaining application IllegalArgumentException cases to BusinessException + ErrorCode.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("BAD_REQUEST", exception.getMessage()));
    }

    // TODO: migrate remaining application IllegalStateException cases to BusinessException + ErrorCode.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("INVALID_STATE", exception.getMessage()));
    }

    // TODO: add an Exception.class fallback handler that logs stack traces and returns a generic INTERNAL_SERVER_ERROR response.
}
