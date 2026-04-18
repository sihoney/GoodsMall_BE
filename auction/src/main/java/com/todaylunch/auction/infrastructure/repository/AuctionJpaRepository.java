package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.Auction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionJpaRepository extends JpaRepository<Auction, UUID> {
}
