package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.common.exception.application.AuctionNotFoundException;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepository {

    private final AuctionJpaRepository jpaRepository;

    @Override
    public Auction findById(UUID auctionId) {
        return jpaRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
    }
}
