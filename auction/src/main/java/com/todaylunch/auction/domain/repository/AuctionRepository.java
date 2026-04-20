package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepository {

    Auction save(Auction auction);

    Auction findById(UUID auctionId);

    Auction findByIdWithLock(UUID auctionId);

    Page<Auction> findAllByStatus(AuctionStatus status, Pageable pageable);
}
