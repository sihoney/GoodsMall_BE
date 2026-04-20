package com.example.payment.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
/**
 * payment 예외 응답의 표준 코드와 기본 메시지를 정의한다.
 * 공통 예외는 이 enum을 기준으로 HTTP 상태와 응답 코드를 일관되게 노출한다.
 */
public enum ErrorCode {

    INVALID_CHARGE_REQUEST(HttpStatus.BAD_REQUEST, "충전 요청이 올바르지 않습니다."),
    INVALID_CARD_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "카드 결제 요청이 올바르지 않습니다."),
    INVALID_ORDER_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "주문 결제 요청이 올바르지 않습니다."),
    INVALID_WITHDRAW_REQUEST(HttpStatus.BAD_REQUEST, "출금 요청이 올바르지 않습니다."),
    INVALID_WITHDRAW_ACCOUNT(HttpStatus.BAD_REQUEST, "출금 계좌 정보가 올바르지 않습니다."),
    WITHDRAW_AMOUNT_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "최소 출금 금액보다 적은 금액은 출금할 수 없습니다."),
    WITHDRAW_AMOUNT_NOT_GREATER_THAN_FEE(HttpStatus.BAD_REQUEST, "출금 금액은 수수료보다 커야 합니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "충전 내역을 찾을 수 없습니다."),
    ESCROW_NOT_FOUND(HttpStatus.NOT_FOUND, "에스크로 정보를 찾을 수 없습니다."),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑 정보를 찾을 수 없습니다."),

    INSUFFICIENT_WALLET_BALANCE(HttpStatus.CONFLICT, "예치금 잔액이 부족합니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없습니다."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "결제 게이트웨이 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
