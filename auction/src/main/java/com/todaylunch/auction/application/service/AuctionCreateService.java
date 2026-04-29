package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.usecase.AuctionCreateUseCase;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.presentation.dto.request.AuctionCreateRequest;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
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
public class AuctionCreateService implements AuctionCreateUseCase {

    private final AuctionRepository auctionRepository;

    @Override
    public AuctionResponse create(UUID sellerId, AuctionCreateRequest request) {
        LocalDateTime scheduledCloseAt = request.startedAt().plusMinutes(request.durationMinutes());

        Auction auction = Auction.create(
                request.productId(),
                request.productTitle(),
                request.thumbnailKey(),
                sellerId,
                request.startPrice(),
                request.bidUnit(),
                request.startedAt(),
                scheduledCloseAt
        );

        Auction saved = auctionRepository.save(auction);

        log.info("Auction created: auctionId={}, productId={}, sellerId={}, startedAt={}, scheduledCloseAt={}",
                saved.getAuctionId(), saved.getProductId(), sellerId, saved.getStartedAt(), saved.getScheduledCloseAt());

        return AuctionResponse.from(saved);
    }
}
