package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.repository.ChargeRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
/**
 * ChargeRepository 포트를 Spring Data JPA로 연결하는 adapter다.
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
    public Charge save(Charge charge) {
        return chargeJpaRepository.save(charge);
    }
}
