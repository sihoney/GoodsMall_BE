package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Wallet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByMemberId(UUID memberId);
}
