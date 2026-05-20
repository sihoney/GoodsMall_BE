package com.example.payment.escrow.infrastructure.repository;

import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.enumtype.EscrowReferenceType;
import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import com.example.payment.escrow.domain.repository.EscrowRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
/**
 * EscrowRepository ?ы듃瑜?Spring Data JPA濡??곌껐?섎뒗 adapter??
 * ?ㅼ쨷 seller 二쇰Ц ?쒕굹由ъ삤??留욎떠 紐⑸줉 ??κ낵 seller 湲곗? 議고쉶瑜??몄텧?쒕떎.
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
    public List<Escrow> saveAll(List<Escrow> escrows) {
        return escrowJpaRepository.saveAll(escrows);
    }

    @Override
    public Optional<Escrow> findByEscrowId(UUID escrowId) {
        return escrowJpaRepository.findById(escrowId);
    }

    @Override
    public List<Escrow> findAllByOrderIdAndSellerMemberId(UUID orderId, UUID sellerMemberId) {
        return escrowJpaRepository.findAllByOrderIdAndSellerMemberId(orderId, sellerMemberId);
    }

    @Override
    public List<Escrow> findAllByOrderId(UUID orderId) {
        return escrowJpaRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<Escrow> lockAllByOrderId(UUID orderId) {
        return escrowJpaRepository.findWithLockByOrderId(orderId);
    }

    @Override
    public List<Escrow> findAllByReferenceTypeAndReferenceIdIn(EscrowReferenceType referenceType, List<UUID> referenceIds) {
        return escrowJpaRepository.findAllByReferenceTypeAndReferenceIdIn(referenceType, referenceIds);
    }

    @Override
    public Page<Escrow> findPendingBySellerMemberId(UUID sellerMemberId, Pageable pageable) {
        return escrowJpaRepository.findBySellerMemberIdAndEscrowStatus(sellerMemberId, EscrowStatus.HELD, pageable);
    }
}
