package com.example.payment.common.exception;

import lombok.Getter;

@Getter
/**
 * payment 공통 예외의 기본 타입이다.
 * presentation 계층이 개별 예외 종류를 직접 알지 않아도 되도록 error code와 메시지를 함께 보관한다.
 */
public abstract class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    protected CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected CustomException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
