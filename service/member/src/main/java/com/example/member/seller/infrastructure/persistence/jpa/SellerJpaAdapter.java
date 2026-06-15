package com.example.member.seller.infrastructure.persistence.jpa;

import com.example.member.seller.application.port.out.SellerPersistencePort;
import com.example.member.seller.domain.entity.Seller;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerJpaAdapter implements SellerPersistencePort {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }

    @Override
    public Optional<Seller> findByMemberId(UUID memberId) {
        return sellerJpaRepository.findByMemberId(memberId);
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return sellerJpaRepository.existsByMemberId(memberId);
    }
}
