package com.example.payment.common.exception;

/**
 * 외부 결제 게이트웨이 호출 또는 응답 해석 실패를 나타내는 공통 예외다.
 */
public class PaymentGatewayException extends CustomException {

    public PaymentGatewayException(String message) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message, cause);
    }
}
