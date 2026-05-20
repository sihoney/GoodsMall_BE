package com.example.payment.auction.domain.repository;

import com.example.payment.auction.domain.entity.AuctionDeposit;
import java.util.Optional;
import java.util.UUID;

public interface AuctionDepositRepository {

    AuctionDeposit save(AuctionDeposit auctionDeposit);

    Optional<AuctionDeposit> findByAuctionDepositId(UUID auctionDepositId);

    Optional<AuctionDeposit> findHeldByAuctionId(UUID auctionId);

    Optional<AuctionDeposit> findHeldByAuctionIdForUpdate(UUID auctionId);

    Optional<AuctionDeposit> findByBidIdForUpdate(UUID bidId);
}
