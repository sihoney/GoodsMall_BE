package com.example.payment.domain.repository;

import com.example.payment.domain.entity.Wallet;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findByWalletId(UUID walletId);

    Optional<Wallet> findByMemberId(UUID memberId);

    Optional<Wallet> findByMemberIdForUpdate(UUID memberId);

    Wallet save(Wallet wallet);
}
