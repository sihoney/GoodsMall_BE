package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidPriceUnitNotMetException extends CustomException {

    public BidPriceUnitNotMetException() {
        super(ErrorCode.BID_PRICE_UNIT_NOT_MET);
    }
}
