package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.usecase.AuctionGetUseCase;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionGetService implements AuctionGetUseCase {

    private final AuctionRepository auctionRepository;

    @Override
    public AuctionResponse get(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId);
        return AuctionResponse.from(auction);
    }
}
