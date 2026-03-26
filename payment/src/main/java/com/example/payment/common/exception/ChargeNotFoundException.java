package com.example.payment.common.exception;

/**
 * 요청한 charge를 찾지 못했을 때 사용하는 공통 예외다.
 */
public class ChargeNotFoundException extends CustomException {

    public ChargeNotFoundException() {
        super(ErrorCode.CHARGE_NOT_FOUND);
    }
}
