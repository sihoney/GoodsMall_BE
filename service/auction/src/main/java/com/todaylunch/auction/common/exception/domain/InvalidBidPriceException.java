package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class InvalidBidPriceException extends CustomException {

    public InvalidBidPriceException() {
        super(ErrorCode.INVALID_BID_PRICE);
    }
}
