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
