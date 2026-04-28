package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.event.OutboxEventPendingTrigger;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.entity.BidPolicy;
import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.domain.repository.OutboxEventRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.AuctionEventTypes;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.AuctionWonPayload;
import com.todaylunch.auction.infrastructure.messaging.kafka.publisher.KafkaAuctionClosedSoldEventPublisher;
import com.todaylunch.auction.infrastructure.messaging.kafka.publisher.KafkaAuctionClosedUnsoldEventPublisher;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaAuctionClosedSoldEventPublisher auctionClosedSoldEventPublisher;
    private final KafkaAuctionClosedUnsoldEventPublisher auctionClosedUnsoldEventPublisher;

    @Transactional
    @Scheduled(fixedRate = 1000 * 10)
    public void startWaitingAuctions() {
        List<Auction> auctions = auctionRepository.findStartable(LocalDateTime.now());
        auctions.forEach(Auction::start);
    }

    @Transactional
    @Scheduled(fixedRate = 1000 * 10)
    public void endExpiredAuctions() {
        List<Auction> auctions = auctionRepository.findEndable(LocalDateTime.now());
        auctions.forEach(this::closeExpiredAuction);
    }

    private void closeExpiredAuction(Auction auction) {
        Auction lockedAuction = auctionRepository.findByIdWithLock(auction.getAuctionId());

        if (lockedAuction.getStatus() != AuctionStatus.ONGOING) {
            return;
        }

        if (lockedAuction.getEndedAt().isAfter(LocalDateTime.now())) {
            return;
        }

        Optional<Bid> bid = bidRepository.findActiveByAuctionId(lockedAuction.getAuctionId());
        if (bid.isPresent()) {
            Bid winningBid = bid.get();
            BigDecimal finalPrice = winningBid.getBidPrice();
            BigDecimal orderPrice = finalPrice.subtract(BidPolicy.calculateBidFee(finalPrice));
            lockedAuction.changeToPendingPayment();
            saveAuctionWonOutboxEvent(lockedAuction, winningBid.getBidderId(), finalPrice, orderPrice);
            auctionClosedSoldEventPublisher.publish(
                    lockedAuction.getAuctionId(),
                    lockedAuction.getSellerId(),
                    lockedAuction.getProductTitle(),
                    winningBid.getBidPrice()
            );
        } else {
            lockedAuction.changeToFailed();
            auctionClosedUnsoldEventPublisher.publish(
                    lockedAuction.getAuctionId(),
                    lockedAuction.getSellerId(),
                    lockedAuction.getProductTitle()
            );
        }
    }

    private void saveAuctionWonOutboxEvent(Auction auction, UUID winnerId, BigDecimal finalPrice, BigDecimal orderPrice) {
        EventEnvelope<AuctionWonPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                AuctionEventTypes.AUCTION_WON,
                "auction-service",
                auction.getAuctionId(),
                winnerId,
                Instant.now(),
                "mock-trace-id",
                new AuctionWonPayload(auction.getProductTitle(), auction.getThumbnailKey(), finalPrice, auction.getProductId(), auction.getSellerId(), orderPrice)
        );
        outboxEventRepository.save(OutboxEvent.create(
                auction.getAuctionId(),
                "AUCTION",
                AuctionEventTypes.AUCTION_WON,
                KafkaTopics.AUCTION_WON,
                auction.getAuctionId().toString(),
                serialize(envelope)
        ));
        eventPublisher.publishEvent(new OutboxEventPendingTrigger());
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new IllegalStateException("Outbox 이벤트 직렬화 실패", e);
        }
    }
}
