package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
            SELECT a FROM Auction a
            WHERE a.sellerId = :sellerId
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<Auction> findAllBySellerIdAndStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status") AuctionStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT a
            FROM Auction a
            WHERE a.auctionId = :auctionId""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Auction> findWithLock(@Param("auctionId") UUID auctionId);

    @Query("""
            SELECT a
            FROM Auction a
            WHERE a.status = 'WAITING' AND a.startedAt <= :now
            """)
    List<Auction> findStartable(LocalDateTime now);

    @Query("""
            SELECT a
            FROM Auction a
            WHERE a.status = 'ONGOING' AND a.endedAt <= :now
            """)
    List<Auction> findEndable(LocalDateTime now);

}
