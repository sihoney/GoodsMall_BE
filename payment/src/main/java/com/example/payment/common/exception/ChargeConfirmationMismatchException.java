package com.example.payment.common.exception;

/**
 * PG 승인 응답이 충전 요청 정보와 일치하지 않을 때 사용한다.
 */
public class ChargeConfirmationMismatchException extends InvalidChargeRequestException {

    public ChargeConfirmationMismatchException(String message) {
        super(message);
    }
}
