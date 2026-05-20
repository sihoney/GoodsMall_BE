package com.example.payment.auction.application.usecase;

import java.util.UUID;

public interface AuctionDepositRefundUseCase {

    void refund(UUID bidId);
}
