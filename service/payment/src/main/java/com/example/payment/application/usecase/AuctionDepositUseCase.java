package com.example.payment.application.usecase;

import com.example.payment.application.dto.AuctionDepositCommand;
import com.example.payment.application.dto.AuctionDepositResult;

public interface AuctionDepositUseCase {

    AuctionDepositResult processAuctionDeposit(AuctionDepositCommand command);
}
