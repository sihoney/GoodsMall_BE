package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.usecase.BidPlaceUseCase;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.presentation.dto.request.BidPlaceRequest;
import com.todaylunch.auction.presentation.dto.response.BidResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidPlaceService implements BidPlaceUseCase {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Override
    public BidResponse place(UUID auctionId, UUID bidderId, BidPlaceRequest request) {
        Auction auction = auctionRepository.findById(auctionId);

        auction.applyConfirmedBid(bidderId, request.bidPrice(), LocalDateTime.now());

        bidRepository.findActiveByAuctionId(auctionId)
                .ifPresent(Bid::outbid);

        Bid bid = Bid.place(auction, bidderId, request.bidPrice());
        Bid saved = bidRepository.save(bid);

        log.info("Bid placed: bidId={}, auctionId={}, bidderId={}, bidPrice={}",
                saved.getBidId(), auctionId, bidderId, saved.getBidPrice());

        return BidResponse.from(saved);
    }
}
