package com.example.notification.presentation.exception;

import com.example.notification.common.exception.NotificationNotFoundException;
import com.example.notification.presentation.dto.ApiResponse;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException exception) {
        // TODO: migrate common-security failures to SecurityException handler.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotificationNotFound(NotificationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("NOTIFICATION_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_INPUT_VALUE", exception.getMessage()));
    }
}
