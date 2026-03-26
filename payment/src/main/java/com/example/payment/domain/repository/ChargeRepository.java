package com.example.payment.domain.repository;

import com.example.payment.domain.entity.Charge;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository {

    Optional<Charge> findByChargeId(UUID chargeId);

    Charge save(Charge charge);
}
