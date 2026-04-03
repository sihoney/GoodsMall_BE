package com.example.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wallet", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 회원의 payment 잔액을 관리하는 wallet aggregate다.
 * 잔액 증감과 최종 balance 반영 규칙을 엔티티 내부에서 보장한다.
 */
public class Wallet {

    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "member_id", nullable = false, unique = true)
    private UUID memberId;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Wallet(UUID walletId, UUID memberId, Long balance, LocalDateTime updatedAt, LocalDateTime createdAt) {
        this.walletId = Objects.requireNonNull(walletId);
        this.memberId = Objects.requireNonNull(memberId);
        this.balance = Objects.requireNonNull(balance);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Wallet create(
            UUID walletId,
            UUID memberId,
            Long balance,
            LocalDateTime updatedAt,
            LocalDateTime createdAt
    ) {
        return new Wallet(walletId, memberId, balance, updatedAt, createdAt);
    }

    /**
     * 외부에서 계산된 최종 balance를 그대로 반영한다.
     */
    public void applyTransaction(Long balanceAfter, LocalDateTime updatedAt) {
        this.balance = Objects.requireNonNull(balanceAfter);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * wallet 잔액을 증가시키고 반영 후 balance를 반환한다.
     */
    public Long increaseBalance(Long amount, LocalDateTime updatedAt) {
        Objects.requireNonNull(amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        this.balance = balance + amount;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        return this.balance;
    }

    /**
     * wallet 잔액을 차감하고 반영 후 balance를 반환한다.
     * 부족한 잔액으로는 차감할 수 없다.
     */
    public Long decreaseBalance(Long amount, LocalDateTime updatedAt) {
        Objects.requireNonNull(amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (balance < amount) {
            throw new IllegalArgumentException("Balance is insufficient.");
        }

        this.balance = balance - amount;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        return this.balance;
    }
}
