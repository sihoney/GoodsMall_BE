package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.WalletTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "wallet_transaction", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private WalletTransactionType transactionType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private WalletTransaction(
            UUID transactionId,
            UUID walletId,
            Long amount,
            Long balanceAfter,
            WalletTransactionType transactionType,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime createdAt
    ) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.walletId = Objects.requireNonNull(walletId);
        this.amount = Objects.requireNonNull(amount);
        this.balanceAfter = Objects.requireNonNull(balanceAfter);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static WalletTransaction create(
            UUID transactionId,
            UUID walletId,
            Long amount,
            Long balanceAfter,
            WalletTransactionType transactionType,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime createdAt
    ) {
        return new WalletTransaction(
                transactionId,
                walletId,
                amount,
                balanceAfter,
                transactionType,
                referenceId,
                referenceType,
                description,
                createdAt
        );
    }

    public static WalletTransaction charge(
            UUID transactionId,
            UUID walletId,
            Long amount,
            Long balanceAfter,
            UUID chargeId,
            LocalDateTime createdAt
    ) {
        if (Objects.requireNonNull(amount) <= 0) {
            throw new IllegalArgumentException("Charge amount must be positive.");
        }

        return create(
                transactionId,
                walletId,
                amount,
                balanceAfter,
                WalletTransactionType.CHARGE,
                chargeId,
                "CHARGE",
                "wallet charge",
                createdAt
        );
    }
}
