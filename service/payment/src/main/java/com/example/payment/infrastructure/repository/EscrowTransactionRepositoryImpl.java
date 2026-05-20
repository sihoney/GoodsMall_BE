package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.EscrowTransaction;
import com.example.payment.domain.repository.EscrowTransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class EscrowTransactionRepositoryImpl implements EscrowTransactionRepository {

    private final EscrowTransactionJpaRepository escrowTransactionJpaRepository;

    public EscrowTransactionRepositoryImpl(EscrowTransactionJpaRepository escrowTransactionJpaRepository) {
        this.escrowTransactionJpaRepository = escrowTransactionJpaRepository;
    }

    @Override
    public EscrowTransaction save(EscrowTransaction escrowTransaction) {
        return escrowTransactionJpaRepository.save(escrowTransaction);
    }

    @Override
    public List<EscrowTransaction> saveAll(List<EscrowTransaction> escrowTransactions) {
        return escrowTransactionJpaRepository.saveAll(escrowTransactions);
    }

    @Override
    public List<EscrowTransaction> findAllByOrderIdOrderByOccurredAtAsc(UUID orderId) {
        return escrowTransactionJpaRepository.findAllByOrderIdOrderByOccurredAtAsc(orderId);
    }

    @Override
    public List<EscrowTransaction> findAllByOrderIdAndSellerMemberIdOrderByOccurredAtAsc(UUID orderId, UUID sellerMemberId) {
        return escrowTransactionJpaRepository.findAllByOrderIdAndSellerMemberIdOrderByOccurredAtAsc(orderId, sellerMemberId);
    }

    @Override
    public List<EscrowTransaction> findAllByEscrowIdOrderByOccurredAtAsc(UUID escrowId) {
        return escrowTransactionJpaRepository.findAllByEscrowIdOrderByOccurredAtAsc(escrowId);
    }
}
