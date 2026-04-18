package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.Bid;
import java.util.Optional;
import java.util.UUID;

public interface BidRepository {

    Bid save(Bid bid);

    Optional<Bid> findActiveByAuctionId(UUID auctionId);
}
