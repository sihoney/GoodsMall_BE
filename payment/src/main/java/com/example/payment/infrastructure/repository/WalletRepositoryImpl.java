package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.repository.WalletRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
/**
 * WalletRepository 포트를 Spring Data JPA로 연결하는 adapter다.
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
    public Wallet save(Wallet wallet) {
        return walletJpaRepository.save(wallet);
    }
}
