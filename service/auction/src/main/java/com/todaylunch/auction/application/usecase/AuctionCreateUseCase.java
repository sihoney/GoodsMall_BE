package com.todaylunch.auction.application.usecase;

import com.todaylunch.auction.presentation.dto.request.AuctionCreateRequest;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import java.util.UUID;

public interface AuctionCreateUseCase {

    AuctionResponse create(UUID sellerId, AuctionCreateRequest request);
}
