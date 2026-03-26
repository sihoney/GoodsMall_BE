package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.EscrowStatus;
import com.example.payment.domain.repository.EscrowRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
/**
 * EscrowRepository 포트를 Spring Data JPA로 연결하는 adapter다.
 * 자동 해제 대상 조회는 HELD + releaseAt 조건으로 한정한다.
 */
public class EscrowRepositoryImpl implements EscrowRepository {

    private final EscrowJpaRepository escrowJpaRepository;

    public EscrowRepositoryImpl(EscrowJpaRepository escrowJpaRepository) {
        this.escrowJpaRepository = escrowJpaRepository;
    }

    @Override
    public Escrow save(Escrow escrow) {
        return escrowJpaRepository.save(escrow);
    }

    @Override
    public Optional<Escrow> findByEscrowId(UUID escrowId) {
        return escrowJpaRepository.findById(escrowId);
    }

    @Override
    public Optional<Escrow> findByOrderId(UUID orderId) {
        return escrowJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Escrow> findReleaseTargets(LocalDateTime releaseAt) {
        return escrowJpaRepository.findByEscrowStatusAndReleaseAtLessThanEqual(EscrowStatus.HELD, releaseAt);
    }
}
