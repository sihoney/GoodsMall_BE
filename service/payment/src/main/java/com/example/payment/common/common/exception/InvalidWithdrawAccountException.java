package com.example.payment.common.common.exception;

public class InvalidWithdrawAccountException extends CustomException {

    public InvalidWithdrawAccountException(String message) {
        super(ErrorCode.INVALID_WITHDRAW_ACCOUNT, message);
    }
}
