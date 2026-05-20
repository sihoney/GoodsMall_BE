package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.Charge;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeJpaRepository extends JpaRepository<Charge, UUID> {

    Optional<Charge> findByPgOrderId(String pgOrderId);

    Optional<Charge> findByChargeIdAndMemberId(UUID chargeId, UUID memberId);

    Page<Charge> findByMemberId(UUID memberId, Pageable pageable);
}
