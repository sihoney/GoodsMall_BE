package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.repository.WalletTransactionRepository;
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
    public Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable) {
        return walletTransactionJpaRepository.findByWalletId(walletId, pageable);
    }
}
