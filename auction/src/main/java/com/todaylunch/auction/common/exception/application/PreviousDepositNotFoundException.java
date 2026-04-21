package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class PreviousDepositNotFoundException extends CustomException {

    public PreviousDepositNotFoundException() {
        super(ErrorCode.PREVIOUS_DEPOSIT_NOT_FOUND);
    }
}
