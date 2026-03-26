package com.example.payment.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_CHARGE_REQUEST(HttpStatus.BAD_REQUEST, "충전 요청이 올바르지 않습니다."),
    INVALID_ORDER_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "주문 결제 요청이 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "충전 내역을 찾을 수 없습니다."),
    ESCROW_NOT_FOUND(HttpStatus.NOT_FOUND, "에스크로 정보를 찾을 수 없습니다."),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑 정보를 찾을 수 없습니다."),

    INVALID_STATE(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없습니다."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "결제 게이트웨이 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
