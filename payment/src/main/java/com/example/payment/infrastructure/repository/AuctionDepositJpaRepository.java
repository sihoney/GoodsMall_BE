package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.AuctionDeposit;
import com.example.payment.domain.enumtype.AuctionDepositStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionDepositJpaRepository extends JpaRepository<AuctionDeposit, UUID> {

    Optional<AuctionDeposit> findByBidId(UUID bidId);

    Optional<AuctionDeposit> findByAuctionIdAndStatus(UUID auctionId, AuctionDepositStatus status);
}
