package com.todaylunch.auction.application.usecase;

import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import com.todaylunch.auction.presentation.dto.response.PagedResponse;
import java.util.UUID;

public interface AuctionSearchUseCase {

    AuctionResponse findById(UUID auctionId);

    PagedResponse<AuctionResponse> search(AuctionStatus status, int page, int size);
}
