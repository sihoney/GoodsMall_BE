package com.example.payment.domain.repository;

import com.example.payment.domain.entity.ChargeRefund;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRefundRepository {

    ChargeRefund save(ChargeRefund chargeRefund);

    boolean existsRefundedByChargeId(UUID chargeId);

    Optional<ChargeRefund> findTopByChargeIdOrderByRequestedAtDesc(UUID chargeId);
}
