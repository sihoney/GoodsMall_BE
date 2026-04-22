package com.todaylunch.auction.application.usecase;

import com.todaylunch.auction.presentation.dto.request.BidPlaceRequest;
import com.todaylunch.auction.presentation.dto.response.BidResponse;
import java.util.UUID;

public interface BidCreateUseCase {

    BidResponse place(UUID auctionId, UUID bidderId, BidPlaceRequest request);
}
