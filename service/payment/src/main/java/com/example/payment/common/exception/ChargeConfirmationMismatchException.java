package com.example.payment.common.exception;

/**
 * PG ?뱀씤 ?묐떟??異⑹쟾 ?붿껌 ?뺣낫? ?쇱튂?섏? ?딆쓣 ???ъ슜?쒕떎.
 */
public class ChargeConfirmationMismatchException extends InvalidChargeRequestException {

    public ChargeConfirmationMismatchException(String message) {
        super(message);
    }
}
