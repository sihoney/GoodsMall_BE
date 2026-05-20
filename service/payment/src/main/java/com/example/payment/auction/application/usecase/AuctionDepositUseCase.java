package com.example.payment.auction.application.usecase;

import com.example.payment.auction.application.dto.AuctionDepositCommand;
import com.example.payment.auction.application.dto.AuctionDepositResult;

public interface AuctionDepositUseCase {

    AuctionDepositResult processAuctionDeposit(AuctionDepositCommand command);
}
