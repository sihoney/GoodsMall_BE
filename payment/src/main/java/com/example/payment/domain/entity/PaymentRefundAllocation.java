package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.PaymentAllocationMethod;
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
@Table(name = "payment_refund_allocation", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefundAllocation {

    @Id
    @Column(name = "refund_allocation_id", nullable = false, updatable = false)
    private UUID refundAllocationId;

    @Column(name = "refund_id", nullable = false, updatable = false)
    private UUID refundId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20, updatable = false)
    private PaymentAllocationMethod method;

    @Column(name = "amount", nullable = false, updatable = false)
    private Long amount;

    @Column(name = "card_cancel_transaction_group_id", updatable = false)
    private UUID cardCancelTransactionGroupId;

    @Column(name = "wallet_refund_transaction_id", updatable = false)
    private UUID walletRefundTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private PaymentRefundAllocation(
            UUID refundAllocationId,
            UUID refundId,
            PaymentAllocationMethod method,
            Long amount,
            UUID cardCancelTransactionGroupId,
            UUID walletRefundTransactionId,
            LocalDateTime createdAt
    ) {
        this.refundAllocationId = Objects.requireNonNull(refundAllocationId);
        this.refundId = Objects.requireNonNull(refundId);
        this.method = Objects.requireNonNull(method);
        this.amount = validatePositiveAmount(amount);
        this.cardCancelTransactionGroupId = cardCancelTransactionGroupId;
        this.walletRefundTransactionId = walletRefundTransactionId;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static PaymentRefundAllocation walletAllocation(
            UUID refundAllocationId,
            UUID refundId,
            Long amount,
            UUID walletRefundTransactionId,
            LocalDateTime createdAt
    ) {
        return new PaymentRefundAllocation(
                refundAllocationId,
                refundId,
                PaymentAllocationMethod.WALLET,
                amount,
                null,
                Objects.requireNonNull(walletRefundTransactionId),
                createdAt
        );
    }

    public static PaymentRefundAllocation cardAllocation(
            UUID refundAllocationId,
            UUID refundId,
            Long amount,
            UUID cardCancelTransactionGroupId,
            LocalDateTime createdAt
    ) {
        return new PaymentRefundAllocation(
                refundAllocationId,
                refundId,
                PaymentAllocationMethod.CARD,
                amount,
                Objects.requireNonNull(cardCancelTransactionGroupId),
                null,
                createdAt
        );
    }

    private static Long validatePositiveAmount(Long amount) {
        if (Objects.requireNonNull(amount) <= 0L) {
            throw new IllegalArgumentException("refund allocation amount must be positive.");
        }
        return amount;
    }
}
