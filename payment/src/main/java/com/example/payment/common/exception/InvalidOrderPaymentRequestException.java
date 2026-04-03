package com.example.payment.common.exception;

/**
 * 주문 결제/escrow 관련 입력이 계약을 만족하지 않을 때 사용하는 공통 예외다.
 */
public class InvalidOrderPaymentRequestException extends CustomException {

    public InvalidOrderPaymentRequestException(String message) {
        super(ErrorCode.INVALID_ORDER_PAYMENT_REQUEST, message);
    }
}
