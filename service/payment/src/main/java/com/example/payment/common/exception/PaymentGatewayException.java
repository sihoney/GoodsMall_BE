package com.example.payment.common.exception;

/**
 * ?몃? 寃곗젣 寃뚯씠?몄썾???몄텧 ?먮뒗 ?묐떟 ?댁꽍 ?ㅽ뙣瑜??섑??대뒗 怨듯넻 ?덉쇅??
 */
public class PaymentGatewayException extends CustomException {

    public PaymentGatewayException(String message) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message, cause);
    }
}
