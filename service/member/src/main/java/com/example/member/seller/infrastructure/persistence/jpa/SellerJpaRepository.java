package com.example.member.seller.infrastructure.persistence.jpa;

import com.example.member.seller.domain.entity.Seller;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<Seller, UUID> {

    Optional<Seller> findByMemberId(UUID memberId);

    boolean existsByMemberId(UUID memberId);
}
