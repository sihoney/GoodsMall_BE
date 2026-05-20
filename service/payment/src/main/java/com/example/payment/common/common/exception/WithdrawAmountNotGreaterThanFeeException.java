package com.example.payment.common.common.exception;

public class WithdrawAmountNotGreaterThanFeeException extends CustomException {

    public WithdrawAmountNotGreaterThanFeeException() {
        super(ErrorCode.WITHDRAW_AMOUNT_NOT_GREATER_THAN_FEE);
    }
}
