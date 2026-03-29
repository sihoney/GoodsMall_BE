package com.example.cartservice.wish.presentation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),

    // 404
    WISH_NOT_FOUND(HttpStatus.NOT_FOUND, "찜을 찾을 수 없습니다"),

    // 403
    MEMBER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 찜에 접근할 권한이 없습니다"),

    // 409
    WISH_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 찜한 상품입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
