package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.AuctionWinAcceptRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;

import java.util.UUID;

public interface AuctionWinAcceptUseCase {

    OrderCreateResponse acceptWinByDeposit(UUID memberId, AuctionWinAcceptRequest request);

    OrderCreateResponse acceptWinByPg(UUID memberId, AuctionWinAcceptRequest request);
}
