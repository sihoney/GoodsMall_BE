package com.example.payment.common.exception;

/**
 * 내부 BigDecimal 금액을 외부 PG 원 단위 금액으로 변환할 수 없을 때 사용한다.
 */
public class PaymentGatewayAmountConversionException extends PaymentGatewayException {

    public PaymentGatewayAmountConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
