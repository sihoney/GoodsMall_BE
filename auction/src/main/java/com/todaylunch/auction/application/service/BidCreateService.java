package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.port.BidFeeChargeEventPublisher;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.application.usecase.BidCreateUseCase;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.entity.BidPolicy;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.presentation.dto.request.BidPlaceRequest;
import com.todaylunch.auction.presentation.dto.response.BidResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidCreateService implements BidCreateUseCase {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final BidFeeChargeEventPublisher bidFeeChargeEventPublisher;

    @Override
    public BidResponse place(UUID auctionId, UUID bidderId, BidPlaceRequest request) {

        Auction auction = auctionRepository.findByIdWithLock(auctionId);
        auction.applyConfirmedBid(bidderId, request.bidPrice(), LocalDateTime.now());

        Optional<Bid> previousBid = bidRepository.findActiveByAuctionId(auctionId);

        Bid bid = Bid.placePending(auction, bidderId, request.bidPrice());
        Bid saved = bidRepository.save(bid);

        // 4. 수수료 계산 및 이벤트 페이로드 구성
        BigDecimal currentBidFee = BidPolicy.calculateBidFee(saved.getBidPrice());
        BidFeeChargeRequest event = new BidFeeChargeRequest(
                saved.getBidId(),
                auction.getAuctionId(),
                previousBid.isEmpty(),
                previousBid.map(Bid::getBidderId).orElse(null),
                previousBid.map(b -> BidPolicy.calculateBidFee(b.getBidPrice())).orElse(null),
                bidderId,
                currentBidFee
        );

        // 5. Kafka로 수수료 차감 요청 이벤트 발행
        bidFeeChargeEventPublisher.publish(event);

        log.info("Bid pending: bidId={}, auctionId={}, bidderId={}, bidPrice={}",
                saved.getBidId(), auctionId, bidderId, saved.getBidPrice());

        return BidResponse.from(saved);
    }

}
