package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowJpaRepository extends JpaRepository<Escrow, UUID> {

    Optional<Escrow> findByOrderId(UUID orderId);

    List<Escrow> findByEscrowStatusAndReleaseAtLessThanEqual(EscrowStatus escrowStatus, LocalDateTime releaseAt);

    Page<Escrow> findBySellerMemberIdAndEscrowStatus(UUID sellerMemberId, EscrowStatus escrowStatus, Pageable pageable);
}
