package com.example.payment.common.common.exception;

/**
 * ?대? BigDecimal 湲덉븸???몃? PG ???⑥쐞 湲덉븸?쇰줈 蹂?섑븷 ???놁쓣 ???ъ슜?쒕떎.
 */
public class PaymentGatewayAmountConversionException extends PaymentGatewayException {

    public PaymentGatewayAmountConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
