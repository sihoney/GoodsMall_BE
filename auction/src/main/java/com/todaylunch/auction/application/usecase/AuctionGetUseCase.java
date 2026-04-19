package com.todaylunch.auction.application.usecase;

import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import java.util.UUID;

public interface AuctionGetUseCase {

    AuctionResponse get(UUID auctionId);
}
