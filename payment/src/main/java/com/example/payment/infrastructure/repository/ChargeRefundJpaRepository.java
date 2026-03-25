package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeRefundJpaRepository extends JpaRepository<ChargeRefund, UUID> {

    boolean existsByChargeIdAndRefundStatus(UUID chargeId, ChargeRefundStatus refundStatus);

    Optional<ChargeRefund> findTopByChargeIdOrderByRequestedAtDesc(UUID chargeId);
}
