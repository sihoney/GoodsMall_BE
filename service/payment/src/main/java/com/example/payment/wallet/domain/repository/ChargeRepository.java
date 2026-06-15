package com.example.payment.wallet.domain.repository;

import com.example.payment.wallet.domain.entity.Charge;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChargeRepository {

    Optional<Charge> findByChargeId(UUID chargeId);

    Optional<Charge> findByPgOrderId(String pgOrderId);

    Optional<Charge> findByChargeIdAndMemberId(UUID chargeId, UUID memberId);

    Page<Charge> findByMemberId(UUID memberId, Pageable pageable);

    Charge save(Charge charge);
}
