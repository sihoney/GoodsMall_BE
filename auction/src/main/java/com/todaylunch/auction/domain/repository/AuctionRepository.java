package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepository {

    Auction save(Auction auction);

    Auction findById(UUID auctionId);

    Auction findByIdWithLock(UUID auctionId);

    Page<Auction> findAllByStatus(AuctionStatus status, Pageable pageable);

    boolean existsBySellerIdAndStatus(UUID sellerId, AuctionStatus status);

    List<Auction> findStartable(LocalDateTime now);

    List<Auction> findEndable(LocalDateTime now);
}
