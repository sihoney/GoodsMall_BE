package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.BidRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BidRepositoryImpl implements BidRepository {

    private final BidJpaRepository jpaRepository;

    @Override
    public Bid save(Bid bid) {
        return jpaRepository.save(bid);
    }

    @Override
    public Optional<Bid> findActiveByAuctionId(UUID auctionId) {
        return jpaRepository.findTopByAuctionIdAndStatus(auctionId, BidStatus.ACTIVE);
    }

    @Override
    public Page<Bid> findAllByAuctionId(UUID auctionId, Pageable pageable) {
        return jpaRepository.findAllByAuctionId(auctionId, pageable);
    }

    @Override
    public Boolean isFirstBid(UUID auctionId) {
        return jpaRepository.findById(auctionId).isPresent();
    }
}
