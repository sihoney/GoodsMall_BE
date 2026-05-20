package com.example.payment.common.exception;

/**
 * 二쇰Ц 寃곗젣/escrow 愿???낅젰??怨꾩빟??留뚯”?섏? ?딆쓣 ???ъ슜?섎뒗 怨듯넻 ?덉쇅??
 */
public class InvalidOrderPaymentRequestException extends CustomException {

    public InvalidOrderPaymentRequestException(String message) {
        super(ErrorCode.INVALID_ORDER_PAYMENT_REQUEST, message);
    }
}
