package com.example.payment.common.common.exception;

/**
 * charge 愿???낅젰??怨꾩빟??留뚯”?섏? ?딆쓣 ???ъ슜?섎뒗 怨듯넻 ?덉쇅??
 */
public class InvalidChargeRequestException extends CustomException {

    public InvalidChargeRequestException(String message) {
        super(ErrorCode.INVALID_CHARGE_REQUEST, message);
    }
}
