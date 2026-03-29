package com.example.settlement.presentation.exception;

import com.example.settlement.presentation.dto.response.ApiResponse;
import com.todaylunch.common.security.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * settlement API에서 발생한 인증 예외를 공통 응답 형식으로 변환한다.
 */
@RestControllerAdvice
public class SettlementExceptionHandler {

    /**
     * 인증 헤더 해석 실패를 401 응답과 {@code INVALID_TOKEN} 오류 코드로 변환한다.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("INVALID_TOKEN", exception.getMessage()));
    }
}


