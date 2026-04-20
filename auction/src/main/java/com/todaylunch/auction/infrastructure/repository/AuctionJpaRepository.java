package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionJpaRepository extends JpaRepository<Auction, UUID> {

    @Query("""
            SELECT a FROM Auction a
            WHERE (:status IS NULL OR a.status = :status)
            """)
    Page<Auction> findAllByStatus(@Param("status") AuctionStatus status, Pageable pageable);

    @Query("select a from Auction a where a.auctionId = :auctionId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Auction> findWithLock(@Param("auctionId") UUID auctionId);
}
