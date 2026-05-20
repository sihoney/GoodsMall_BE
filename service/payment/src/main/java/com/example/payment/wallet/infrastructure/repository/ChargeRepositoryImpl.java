package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.Charge;
import com.example.payment.wallet.domain.repository.ChargeRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
/**
 * ChargeRepository ?ы듃瑜?Spring Data JPA濡??곌껐?섎뒗 adapter??
 */
public class ChargeRepositoryImpl implements ChargeRepository {

    private final ChargeJpaRepository chargeJpaRepository;

    public ChargeRepositoryImpl(ChargeJpaRepository chargeJpaRepository) {
        this.chargeJpaRepository = chargeJpaRepository;
    }

    @Override
    public Optional<Charge> findByChargeId(UUID chargeId) {
        return chargeJpaRepository.findById(chargeId);
    }

    @Override
    public Optional<Charge> findByPgOrderId(String pgOrderId) {
        return chargeJpaRepository.findByPgOrderId(pgOrderId);
    }

    @Override
    public Optional<Charge> findByChargeIdAndMemberId(UUID chargeId, UUID memberId) {
        return chargeJpaRepository.findByChargeIdAndMemberId(chargeId, memberId);
    }

    @Override
    public Page<Charge> findByMemberId(UUID memberId, Pageable pageable) {
        return chargeJpaRepository.findByMemberId(memberId, pageable);
    }

    @Override
    public Charge save(Charge charge) {
        return chargeJpaRepository.save(charge);
    }
}
