package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.Auction;
import java.util.UUID;

public interface AuctionRepository {

    Auction save(Auction auction);

    Auction findById(UUID auctionId);
}
