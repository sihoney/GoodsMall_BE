package com.example.payment.common.exception;

/**
 * 외부 결제 게이트웨이 호출 또는 응답 해석 실패를 나타내는 공통 예외다.
 */
// TODO: PaymentGatewayException 내부 원인 분류(설정 오류 / 4xx / 5xx / 네트워크 / 파싱) 필요 여부 검토
public class PaymentGatewayException extends CustomException {

    public PaymentGatewayException(String message) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message, cause);
    }
}
