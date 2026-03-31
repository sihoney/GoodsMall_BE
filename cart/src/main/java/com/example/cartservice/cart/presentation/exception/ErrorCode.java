package com.example.cartservice.cart.presentation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),

    // 404
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다"),

    // 409
    CART_ITEM_DUPLICATE(HttpStatus.CONFLICT, "이미 장바구니에 있는 상품입니다"),
    CART_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "장바구니에는 최대 10개까지 담을 수 있습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
