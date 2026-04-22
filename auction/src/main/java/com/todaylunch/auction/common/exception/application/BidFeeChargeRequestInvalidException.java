package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidFeeChargeRequestInvalidException extends CustomException {

    public BidFeeChargeRequestInvalidException() {
        super(ErrorCode.BID_FEE_CHARGE_REQUEST_INVALID);
    }
}
