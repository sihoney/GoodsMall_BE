package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.repository.WalletRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
/**
 * WalletRepository ?ы듃瑜?Spring Data JPA濡??곌껐?섎뒗 adapter??
 */
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    public WalletRepositoryImpl(WalletJpaRepository walletJpaRepository) {
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public Optional<Wallet> findByWalletId(UUID walletId) {
        return walletJpaRepository.findById(walletId);
    }

    @Override
    public Optional<Wallet> findByMemberId(UUID memberId) {
        return walletJpaRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<Wallet> findByMemberIdForUpdate(UUID memberId) {
        return walletJpaRepository.findByMemberIdForUpdate(memberId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return walletJpaRepository.save(wallet);
    }
}
