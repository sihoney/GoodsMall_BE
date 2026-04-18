package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.WithdrawStatus;
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
@Table(name = "withdraw_request", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawRequest {

    @Id
    @Column(name = "withdraw_request_id", nullable = false, updatable = false)
    private UUID withdrawRequestId;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "fee", nullable = false)
    private Long fee;

    @Column(name = "actual_amount", nullable = false)
    private Long actualAmount;

    @Column(name = "bank_account", nullable = false)
    private String bankAccount;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WithdrawStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "wallet_transaction_id")
    private UUID walletTransactionId;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private WithdrawRequest(
            UUID withdrawRequestId,
            UUID memberId,
            UUID walletId,
            Long amount,
            Long fee,
            Long actualAmount,
            String bankAccount,
            String accountHolder,
            WithdrawStatus status,
            String failureReason,
            UUID walletTransactionId,
            LocalDateTime requestedAt,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.withdrawRequestId = Objects.requireNonNull(withdrawRequestId);
        this.memberId = Objects.requireNonNull(memberId);
        this.walletId = Objects.requireNonNull(walletId);
        this.amount = Objects.requireNonNull(amount);
        this.fee = Objects.requireNonNull(fee);
        this.actualAmount = Objects.requireNonNull(actualAmount);
        this.bankAccount = Objects.requireNonNull(bankAccount);
        this.accountHolder = Objects.requireNonNull(accountHolder);
        this.status = Objects.requireNonNull(status);
        this.failureReason = failureReason;
        this.walletTransactionId = walletTransactionId;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.processedAt = processedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static WithdrawRequest createRequested(
            UUID withdrawRequestId,
            UUID memberId,
            UUID walletId,
            Long amount,
            Long fee,
            Long actualAmount,
            String bankAccount,
            String accountHolder,
            LocalDateTime requestedAt
    ) {
        validatePositiveAmount(amount, "withdraw amount must be positive.");
        validateNonNegativeFee(fee);
        if (actualAmount == null || actualAmount <= 0) {
            throw new IllegalArgumentException("actualAmount must be positive.");
        }

        return new WithdrawRequest(
                withdrawRequestId,
                memberId,
                walletId,
                amount,
                fee,
                actualAmount,
                bankAccount,
                accountHolder,
                WithdrawStatus.REQUESTED,
                null,
                null,
                requestedAt,
                null,
                requestedAt,
                requestedAt
        );
    }

    public void linkWalletTransaction(UUID walletTransactionId, LocalDateTime updatedAt) {
        this.walletTransactionId = Objects.requireNonNull(walletTransactionId);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void complete(LocalDateTime processedAt) {
        this.status = WithdrawStatus.COMPLETED;
        this.processedAt = Objects.requireNonNull(processedAt);
        this.updatedAt = processedAt;
        this.failureReason = null;
    }

    public void fail(String failureReason, LocalDateTime processedAt) {
        this.status = WithdrawStatus.FAILED;
        this.failureReason = failureReason;
        this.processedAt = Objects.requireNonNull(processedAt);
        this.updatedAt = processedAt;
    }

    private static void validatePositiveAmount(Long amount, String message) {
        if (Objects.requireNonNull(amount) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNonNegativeFee(Long fee) {
        if (Objects.requireNonNull(fee) < 0) {
            throw new IllegalArgumentException("fee must be zero or positive.");
        }
    }
}
