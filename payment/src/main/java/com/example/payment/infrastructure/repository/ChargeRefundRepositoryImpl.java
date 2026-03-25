package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import com.example.payment.domain.repository.ChargeRefundRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ChargeRefundRepositoryImpl implements ChargeRefundRepository {

    private final ChargeRefundJpaRepository chargeRefundJpaRepository;

    public ChargeRefundRepositoryImpl(ChargeRefundJpaRepository chargeRefundJpaRepository) {
        this.chargeRefundJpaRepository = chargeRefundJpaRepository;
    }

    @Override
    public ChargeRefund save(ChargeRefund chargeRefund) {
        return chargeRefundJpaRepository.save(chargeRefund);
    }

    @Override
    public boolean existsRefundedByChargeId(UUID chargeId) {
        return chargeRefundJpaRepository.existsByChargeIdAndRefundStatus(chargeId, ChargeRefundStatus.REFUNDED);
    }

    @Override
    public Optional<ChargeRefund> findTopByChargeIdOrderByRequestedAtDesc(UUID chargeId) {
        return chargeRefundJpaRepository.findTopByChargeIdOrderByRequestedAtDesc(chargeId);
    }
}
