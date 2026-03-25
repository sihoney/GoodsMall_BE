package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.WalletTransaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction, UUID> {
}
