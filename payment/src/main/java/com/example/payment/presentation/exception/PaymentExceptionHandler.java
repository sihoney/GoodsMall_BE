package com.example.payment.presentation.exception;

import com.example.payment.domain.exception.InvalidChargeRequestException;
import com.example.payment.domain.exception.EscrowNotFoundException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.presentation.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler({
            InvalidChargeRequestException.class,
            InvalidOrderPaymentRequestException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWalletNotFound(WalletNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("WALLET_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(EscrowNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEscrowNotFound(EscrowNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("ESCROW_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "Invalid request." : error.getDefaultMessage())
                .orElse("Invalid request.");

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of("VALIDATION_ERROR", message));
    }
}
