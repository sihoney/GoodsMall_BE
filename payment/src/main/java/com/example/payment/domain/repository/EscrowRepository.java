package com.example.payment.domain.repository;

import com.example.payment.domain.entity.Escrow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EscrowRepository {

    Escrow save(Escrow escrow);

    Optional<Escrow> findByEscrowId(UUID escrowId);

    Optional<Escrow> findByOrderId(UUID orderId);

    List<Escrow> findReleaseTargets(LocalDateTime releaseAt);

    Page<Escrow> findPendingBySellerMemberId(UUID sellerMemberId, Pageable pageable);
}
