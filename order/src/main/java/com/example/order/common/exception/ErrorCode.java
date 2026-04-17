package com.example.order.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "내부 서버 오류입니다."),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "COMMON_502_1", "외부 서비스 호출 중 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_1", "잘못된 입력입니다."),

    // 회원
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404_1", "상품이 존재하지 않습니다."),
    PRODUCT_NOT_ORDERABLE(HttpStatus.BAD_REQUEST, "PRODUCT_400_1", "주문할 수 없는 상품입니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_400_2", "재고가 부족합니다."),

    // 주문
    DUPLICATE_PRODUCT_REQUEST(HttpStatus.BAD_REQUEST, "ORDER_400_1", "상품이 중복되었습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_1", "주문이 존재하지 않습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER_403_1", "해당 주문에 대한 권한이 없습니다."),

    // 배송
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY_404_1", "배송이 존재하지 않습니다."),

    // 결제
    INVALID_PAYMENT_AMOUNT(HttpStatus.CONFLICT, "PAYMENT_409_1", "결제 금액이 주문 금액과 일치하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "유효하지 않은 결제 상태입니다."),
    PAYMENT_FAILED(HttpStatus.CONFLICT, "PAYMENT_409_3", "결제에 실패했습니다."),
    REFUND_FAILED(HttpStatus.CONFLICT, "PAYMENT_409_4", "환불 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
