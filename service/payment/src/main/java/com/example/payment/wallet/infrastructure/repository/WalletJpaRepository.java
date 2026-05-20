package com.example.payment.wallet.infrastructure.repository;

import com.example.payment.wallet.domain.entity.Wallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByMemberId(UUID memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from Wallet wallet where wallet.memberId = :memberId")
    Optional<Wallet> findByMemberIdForUpdate(@Param("memberId") UUID memberId);
}
