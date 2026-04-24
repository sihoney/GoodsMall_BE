package com.todaylunch.auction.application.service;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
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

    @Scheduled(fixedRate = 1000 * 10)
    @Transactional
    public void startWaitingAuctions() {
        List<Auction> auctions = auctionRepository.findStartable(LocalDateTime.now());
        auctions.forEach(Auction::start);
    }

    @Scheduled(fixedRate = 1000 * 10)
    @Transactional
    public void endExpiredAuctions() {
        List<Auction> auctions = auctionRepository.findEndable(LocalDateTime.now());
        auctions.forEach(this::closeExpiredAuction);
    }

    private void closeExpiredAuction(Auction auction) {
        Optional<Bid> bid = bidRepository.findActiveByAuctionId(auction.getAuctionId());
        if (bid.isPresent()) {
            Bid winningBid = bid.get();
            auction.changeToPendingPayment();
            auctionWonEventPublisher.publish(
                    auction.getAuctionId(),
                    winningBid.getBidderId(),
                    auction.getProductTitle(),
                    winningBid.getBidPrice()
            );
            auctionClosedSoldEventPublisher.publish(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    auction.getProductTitle(),
                    winningBid.getBidPrice()
            );
        } else {
            auction.changeToFailed();
            auctionClosedUnsoldEventPublisher.publish(
                    auction.getAuctionId(),
                    auction.getSellerId(),
                    auction.getProductTitle()
            );
        }
    }
}
