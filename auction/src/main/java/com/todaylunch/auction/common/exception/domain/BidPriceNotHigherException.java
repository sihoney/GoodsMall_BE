package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidPriceNotHigherException extends CustomException {

    public BidPriceNotHigherException() {
        super(ErrorCode.BID_PRICE_NOT_HIGHER);
    }
}
