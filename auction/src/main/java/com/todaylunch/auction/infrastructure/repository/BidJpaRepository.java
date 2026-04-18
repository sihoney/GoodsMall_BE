package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidJpaRepository extends JpaRepository<Bid, UUID> {

    @Query("""
            select b
            from Bid b
            where b.auction.auctionId = :auctionId
              and b.status = :status
            order by b.bidPrice desc
            limit 1
            """)
    Optional<Bid> findTopByAuctionIdAndStatus(
            @Param("auctionId") UUID auctionId,
            @Param("status") BidStatus status
    );
}
