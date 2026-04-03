package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.Seller;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }

    public Optional<Seller> findByMemberId(UUID memberId) {
        return sellerJpaRepository.findByMemberId(memberId);
    }

    public boolean existsByMemberId(UUID memberId) {
        return sellerJpaRepository.existsByMemberId(memberId);
    }
}
