package com.example.payment.common.exception;

public class EscrowNotFoundException extends CustomException {

    public EscrowNotFoundException() {
        super(ErrorCode.ESCROW_NOT_FOUND);
    }
}
