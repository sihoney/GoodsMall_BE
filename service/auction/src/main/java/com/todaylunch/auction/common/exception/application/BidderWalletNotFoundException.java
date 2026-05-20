package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidderWalletNotFoundException extends CustomException {

    public BidderWalletNotFoundException() {
        super(ErrorCode.BIDDER_WALLET_NOT_FOUND);
    }
}
