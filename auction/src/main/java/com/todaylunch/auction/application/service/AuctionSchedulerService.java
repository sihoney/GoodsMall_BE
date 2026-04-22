package com.todaylunch.auction.application.service;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Scheduled(fixedRate = 1000 * 10)
    public void startWaitingActions(){
        List<Auction> actions = auctionRepository.findStartable(LocalDateTime.now());
        actions.forEach(Auction::start);
    }

    @Scheduled(fixedRate = 1000 * 10)
    public void endWaitingActions(){
        List<Auction> actions = auctionRepository.findEndable(LocalDateTime.now());
        endExpiredAuctions(actions);
    }

    private void endExpiredAuctions(List<Auction> actions) {
        for (Auction auction : actions) {
            Optional<Bid> bid = bidRepository.findActiveByAuctionId(auction.getAuctionId());
            if (bid.isPresent()) {
                auction.changeToPendingPayment();
                continue;
            }
            auction.changeToFailed();
        }
    }


}
