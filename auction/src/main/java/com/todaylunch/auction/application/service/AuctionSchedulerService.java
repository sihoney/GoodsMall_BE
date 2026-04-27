package com.todaylunch.auction.application.service;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.publisher.KafkaAuctionClosedSoldEventPublisher;
import com.todaylunch.auction.infrastructure.messaging.kafka.publisher.KafkaAuctionClosedUnsoldEventPublisher;
import com.todaylunch.auction.infrastructure.messaging.kafka.publisher.KafkaAuctionWonEventPublisher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final KafkaAuctionWonEventPublisher auctionWonEventPublisher;
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
            lockedAuction.changeToPendingPayment();
            auctionWonEventPublisher.publish(
                    lockedAuction.getAuctionId(),
                    winningBid.getBidderId(),
                    lockedAuction.getProductTitle(),
                    winningBid.getBidPrice()
            );
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
}
