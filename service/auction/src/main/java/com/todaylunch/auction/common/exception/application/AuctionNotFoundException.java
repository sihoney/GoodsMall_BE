package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class AuctionNotFoundException extends CustomException {

    public AuctionNotFoundException() {
        super(ErrorCode.AUCTION_NOT_FOUND);
    }
}
