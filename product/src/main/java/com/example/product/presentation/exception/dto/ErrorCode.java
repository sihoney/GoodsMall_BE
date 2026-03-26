package com.example.product.presentation.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //404
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다"),
    SELLER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 상품에 대한 권한이 없습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
