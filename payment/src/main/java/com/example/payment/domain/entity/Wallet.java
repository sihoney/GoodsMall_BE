package com.example.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cretead_at")
    private LocalDateTime createdAt;

    private Wallet(UUID walletId, UUID memberId, BigDecimal balance, LocalDateTime updatedAt, LocalDateTime createdAt) {
        this.walletId = Objects.requireNonNull(walletId);
        this.memberId = Objects.requireNonNull(memberId);
        this.balance = Objects.requireNonNull(balance);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.createdAt = createdAt;
    }

    public static Wallet create(
            UUID walletId,
            UUID memberId,
            BigDecimal balance,
            LocalDateTime updatedAt,
            LocalDateTime createdAt
    ) {
        return new Wallet(walletId, memberId, balance, updatedAt, createdAt);
    }

    public void applyTransaction(BigDecimal balanceAfter, LocalDateTime updatedAt) {
        this.balance = Objects.requireNonNull(balanceAfter);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
