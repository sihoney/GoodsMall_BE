package com.example.cartservice.cart.presentation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 최소 1개 이상이어야 합니다"),

    // 404
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다"),

    // 403
    MEMBER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 장바구니에 접근할 권한이 없습니다"),

    // 409
    CART_ITEM_DUPLICATE(HttpStatus.CONFLICT, "이미 장바구니에 있는 상품입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
