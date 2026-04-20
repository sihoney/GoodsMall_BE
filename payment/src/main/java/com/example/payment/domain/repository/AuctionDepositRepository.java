package com.example.payment.domain.repository;

import com.example.payment.domain.entity.AuctionDeposit;
import java.util.Optional;
import java.util.UUID;

public interface AuctionDepositRepository {

    AuctionDeposit save(AuctionDeposit auctionDeposit);

    Optional<AuctionDeposit> findByAuctionDepositId(UUID auctionDepositId);

    Optional<AuctionDeposit> findByBidId(UUID bidId);

    Optional<AuctionDeposit> findHeldByAuctionId(UUID auctionId);
}
