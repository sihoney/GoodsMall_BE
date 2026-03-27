package com.example.payment.domain.repository;

import com.example.payment.domain.entity.WalletTransaction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction walletTransaction);

    Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable);
}
