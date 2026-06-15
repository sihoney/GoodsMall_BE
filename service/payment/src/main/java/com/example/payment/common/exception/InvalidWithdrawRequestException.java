package com.example.payment.common.exception;

public class InvalidWithdrawRequestException extends CustomException {

    public InvalidWithdrawRequestException(String message) {
        super(ErrorCode.INVALID_WITHDRAW_REQUEST, message);
    }
}
