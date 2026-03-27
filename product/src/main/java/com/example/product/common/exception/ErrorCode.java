package com.example.product.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    CATEGORY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "소분류 이하로는 카테고리를 생성할 수 없습니다"),
    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "하위 카테고리가 존재하여 삭제할 수 없습니다"),

    //404
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다"),

    //403
    SELLER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 상품에 대한 권한이 없습니다"),
    UNAUTHORIZED_ROLE(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다"),
    SELLER_CANNOT_CREATE_ROOT_CATEGORY(HttpStatus.FORBIDDEN, "판매자는 대분류를 생성할 수 없습니다"),

    //409
    PRODUCT_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 상품입니다"),
    CATEGORY_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 카테고리입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
