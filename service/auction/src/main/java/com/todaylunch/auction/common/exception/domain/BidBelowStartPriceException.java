package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidBelowStartPriceException extends CustomException {

    public BidBelowStartPriceException() {
        super(ErrorCode.BID_BELOW_START_PRICE);
    }
}
