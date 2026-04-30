package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.common.exception.application.AuctionNotFoundException;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepository {

    private final AuctionJpaRepository jpaRepository;

    @Override
    public Auction save(Auction auction) {
        return jpaRepository.save(auction);
    }

    @Override
    public Auction findById(UUID auctionId) {
        return jpaRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
    }

    @Override
    public List<Auction> findStartable(LocalDateTime now) {
        return jpaRepository.findStartable(now);
    }

    @Override
    public List<Auction> findEndable(LocalDateTime now) {
        return jpaRepository.findEndable(now);
    }

    @Override
    public Auction findByIdWithLock(UUID auctionId) {
        return jpaRepository.findWithLock(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
    }

    @Override
    public Page<Auction> findAllByStatus(AuctionStatus status, Pageable pageable) {
        return jpaRepository.findAllByStatus(status, pageable);
    }

    @Override
    public Page<Auction> findAllBySellerIdAndStatus(UUID sellerId, AuctionStatus status, Pageable pageable) {
        return jpaRepository.findAllBySellerIdAndStatus(sellerId, status, pageable);
    }
}
