package com.example.payment.presentation.exception;

import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.presentation.dto.response.ApiResponse;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * payment API 예외를 공통 응답 형식으로 변환한다.
 * 공통 예외는 ErrorCode를 그대로 사용하고, 인증/입력/상태 가드는 별도 코드로 매핑한다.
 */
@RestControllerAdvice
public class PaymentExceptionHandler {

    /**
     * 인증 헤더 해석 실패를 401 응답과 {@code INVALID_TOKEN} 오류 코드로 변환한다.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException exception) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }

    /**
     * payment 공통 예외를 ErrorCode 기반 응답으로 변환한다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getErrorCode().name(), e.getMessage()));
    }

    /**
     * domain/application의 최소 guard 예외를 공통 API 코드로 변환한다.
     * IllegalArgumentException은 입력 오류, IllegalStateException은 상태 충돌로 구분한다.
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleRuntimeStateException(RuntimeException e) {
        ErrorCode errorCode = e instanceof IllegalStateException
                ? ErrorCode.INVALID_STATE
                : ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.name(), e.getMessage()));
    }

    /**
     * Bean Validation 실패를 첫 번째 필드 오류 기준으로 응답한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "잘못된 요청입니다." : error.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.name(), message));
    }
}
