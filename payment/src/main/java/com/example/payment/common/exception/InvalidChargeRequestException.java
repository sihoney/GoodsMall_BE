package com.example.payment.common.exception;

/**
 * charge 관련 입력이 계약을 만족하지 않을 때 사용하는 공통 예외다.
 */
public class InvalidChargeRequestException extends CustomException {

    public InvalidChargeRequestException(String message) {
        super(ErrorCode.INVALID_CHARGE_REQUEST, message);
    }
}
