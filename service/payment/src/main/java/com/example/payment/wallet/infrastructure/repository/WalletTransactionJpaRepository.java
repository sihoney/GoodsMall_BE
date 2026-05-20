package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.WalletTransaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction, UUID> {

    Optional<WalletTransaction> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable);
}
