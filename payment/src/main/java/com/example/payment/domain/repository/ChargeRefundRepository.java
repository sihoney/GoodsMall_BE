package com.example.payment.domain.repository;

import com.example.payment.domain.entity.ChargeRefund;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChargeRefundRepository {

    ChargeRefund save(ChargeRefund chargeRefund);

    boolean existsRefundedByChargeId(UUID chargeId);

    Optional<ChargeRefund> findTopByChargeIdOrderByRequestedAtDesc(UUID chargeId);

    Page<ChargeRefund> findByMemberId(UUID memberId, Pageable pageable);
}
