package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.event.BidPlacedEvent;
import com.todaylunch.auction.application.port.BidFeeChargePort;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.application.port.dto.response.BidFeeChargeResponse;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidCreateService implements BidCreateUseCase {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final BidFeeChargePort bidFeeChargePort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public BidResponse place(UUID auctionId, UUID bidderId, BidPlaceRequest request) {

        // 1. 검증
        Auction auction = auctionRepository.findByIdWithLock(auctionId);
        auction.applyConfirmedBid(bidderId, request.bidPrice(), LocalDateTime.now());

        // 2. 최초 입찰인지 조회
        Optional<Bid> previousBid = bidRepository.findActiveByAuctionId(auctionId);

        // 3. Pending 상태로 입찰 생성
        Bid bid = Bid.placePending(auction, bidderId, request.bidPrice());
        Bid saved = bidRepository.save(bid);

        // 4. 수수료 계산 및 payment 요청 구성
        BigDecimal currentBidFee = BidPolicy.calculateBidFee(bid.getBidPrice());
        BidFeeChargeRequest clientRequest = new BidFeeChargeRequest(
                auction.getAuctionId(),
                previousBid.isEmpty(),
                previousBid.map(Bid::getBidderId).orElse(null),
                previousBid.map(b -> BidPolicy.calculateBidFee(b.getBidPrice())).orElse(null),
                bidderId,
                currentBidFee
        );

        // 5. 예치금 차감 호출
        BidFeeChargeResponse chargeResponse = bidFeeChargePort.chargeBidFee(clientRequest);

        // 6. 결제 확정 이후 상태 전이
        saved.confirm();
        previousBid.ifPresent(Bid::outbid);

        // 7. 실시간 브로드캐스트
        applicationEventPublisher.publishEvent(new BidPlacedEvent(
                auction.getAuctionId(),
                bid.getBidderId(),
                bid.getBidPrice(),
                auction.getEndedAt()
        ));

        log.info("Bid placed: bidId={}, auctionId={}, bidderId={}, bidPrice={}",
                saved.getBidId(), auctionId, bidderId, saved.getBidPrice());

        return BidResponse.from(saved);
    }

}
