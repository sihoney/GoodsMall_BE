package com.example.payment.presentation.exception;

import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.presentation.dto.response.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * payment API 예외를 공통 응답 형식으로 변환한다.
 * 공통 예외는 ErrorCode를 그대로 사용하고, 구조적 state/input guard는 별도 코드로 매핑한다.
 */
public class PaymentExceptionHandler {

    @ExceptionHandler(CustomException.class)
    /**
     * payment 공통 예외를 ErrorCode 기반 응답으로 변환한다.
     */
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiErrorResponse.of(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    /**
     * domain/application의 최소 guard 예외를 공통 API 코드로 변환한다.
     * IllegalArgumentException은 입력 오류, IllegalStateException은 상태 충돌로 구분한다.
     */
    public ResponseEntity<ApiErrorResponse> handleRuntimeStateException(RuntimeException e) {
        ErrorCode errorCode = e instanceof IllegalStateException
                ? ErrorCode.INVALID_STATE
                : ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.of(errorCode.name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Bean Validation 실패를 첫 번째 필드 오류 기준으로 응답한다.
     */
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "Invalid request." : error.getDefaultMessage())
                .orElse("Invalid request.");

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE.name(), message));
    }
}
