package com.example.member.seller.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SellerErrorCode implements ErrorCode {
    SELLER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SELLER_ALREADY_REGISTERED", "이미 판매자로 등록된 회원입니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "판매자 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    SellerErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
