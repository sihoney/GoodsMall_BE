package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.AuctionWinAcceptRequest;

import java.util.UUID;

public interface AuctionWinAcceptUseCase {

    void acceptWinByDeposit(UUID memberId, AuctionWinAcceptRequest request);

    void acceptWinByPg(UUID memberId, AuctionWinAcceptRequest request);
}
