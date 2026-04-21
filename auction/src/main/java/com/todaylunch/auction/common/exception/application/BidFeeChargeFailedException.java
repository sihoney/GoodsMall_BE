package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidFeeChargeFailedException extends CustomException {

    public BidFeeChargeFailedException() {
        super(ErrorCode.BID_FEE_CHARGE_FAILED);
    }
}
