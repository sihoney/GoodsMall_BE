package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class WalletTransactionRepositoryImpl implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository walletTransactionJpaRepository;

    public WalletTransactionRepositoryImpl(WalletTransactionJpaRepository walletTransactionJpaRepository) {
        this.walletTransactionJpaRepository = walletTransactionJpaRepository;
    }

    @Override
    public WalletTransaction save(WalletTransaction walletTransaction) {
        return walletTransactionJpaRepository.save(walletTransaction);
    }

    @Override
    public Optional<WalletTransaction> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType) {
        return walletTransactionJpaRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }

    @Override
    public Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable) {
        return walletTransactionJpaRepository.findByWalletId(walletId, pageable);
    }
}
