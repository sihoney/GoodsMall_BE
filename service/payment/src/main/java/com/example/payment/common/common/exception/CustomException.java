package com.example.payment.common.common.exception;

import lombok.Getter;

@Getter
/**
 * payment 怨듯넻 ?덉쇅??湲곕낯 ??낆씠??
 * presentation 怨꾩링??媛쒕퀎 ?덉쇅 醫낅쪟瑜?吏곸젒 ?뚯? ?딆븘???섎룄濡?error code? 硫붿떆吏瑜??④퍡 蹂닿??쒕떎.
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
