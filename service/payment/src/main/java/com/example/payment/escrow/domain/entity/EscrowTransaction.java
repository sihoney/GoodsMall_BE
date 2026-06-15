package com.example.payment.escrow.domain.entity;

import com.example.payment.escrow.domain.enumtype.EscrowTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "escrow_transaction", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EscrowTransaction {

    @Id
    @Column(name = "escrow_transaction_id", nullable = false, updatable = false)
    private UUID escrowTransactionId;

    @Column(name = "escrow_id", nullable = false, updatable = false)
    private UUID escrowId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "order_item_id", updatable = false)
    private UUID orderItemId;

    @Column(name = "seller_member_id", nullable = false, updatable = false)
    private UUID sellerMemberId;

    @Column(name = "buyer_member_id", nullable = false, updatable = false)
    private UUID buyerMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false)
    private EscrowTransactionType transactionType;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "before_amount", nullable = false, updatable = false)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", nullable = false, updatable = false)
    private BigDecimal afterAmount;

    @Column(name = "reference_id", updatable = false)
    private UUID referenceId;

    @Column(name = "reference_type", updatable = false)
    private String referenceType;

    @Column(name = "description", updatable = false)
    private String description;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private EscrowTransaction(
            UUID escrowTransactionId,
            UUID escrowId,
            UUID orderId,
            UUID orderItemId,
            UUID sellerMemberId,
            UUID buyerMemberId,
            EscrowTransactionType transactionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        this.escrowTransactionId = Objects.requireNonNull(escrowTransactionId);
        this.escrowId = Objects.requireNonNull(escrowId);
        this.orderId = Objects.requireNonNull(orderId);
        this.orderItemId = orderItemId;
        this.sellerMemberId = Objects.requireNonNull(sellerMemberId);
        this.buyerMemberId = Objects.requireNonNull(buyerMemberId);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.amount = validatePositiveAmount(amount);
        this.beforeAmount = validateNonNegativeAmount(beforeAmount);
        this.afterAmount = validateNonNegativeAmount(afterAmount);
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.description = description;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static EscrowTransaction create(
            UUID escrowTransactionId,
            UUID escrowId,
            UUID orderId,
            UUID orderItemId,
            UUID sellerMemberId,
            UUID buyerMemberId,
            EscrowTransactionType transactionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        return new EscrowTransaction(
                escrowTransactionId,
                escrowId,
                orderId,
                orderItemId,
                sellerMemberId,
                buyerMemberId,
                transactionType,
                amount,
                beforeAmount,
                afterAmount,
                referenceId,
                referenceType,
                description,
                occurredAt,
                createdAt
        );
    }

    public static EscrowTransaction hold(
            UUID escrowTransactionId,
            UUID escrowId,
            UUID orderId,
            UUID orderItemId,
            UUID sellerMemberId,
            UUID buyerMemberId,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        return create(
                escrowTransactionId,
                escrowId,
                orderId,
                orderItemId,
                sellerMemberId,
                buyerMemberId,
                EscrowTransactionType.HOLD,
                amount,
                beforeAmount,
                afterAmount,
                referenceId,
                referenceType,
                description,
                occurredAt,
                createdAt
        );
    }

    public static EscrowTransaction refund(
            UUID escrowTransactionId,
            UUID escrowId,
            UUID orderId,
            UUID orderItemId,
            UUID sellerMemberId,
            UUID buyerMemberId,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        return create(
                escrowTransactionId,
                escrowId,
                orderId,
                orderItemId,
                sellerMemberId,
                buyerMemberId,
                EscrowTransactionType.REFUND,
                amount,
                beforeAmount,
                afterAmount,
                referenceId,
                referenceType,
                description,
                occurredAt,
                createdAt
        );
    }

    public static EscrowTransaction release(
            UUID escrowTransactionId,
            UUID escrowId,
            UUID orderId,
            UUID orderItemId,
            UUID sellerMemberId,
            UUID buyerMemberId,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            UUID referenceId,
            String referenceType,
            String description,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        return create(
                escrowTransactionId,
                escrowId,
                orderId,
                orderItemId,
                sellerMemberId,
                buyerMemberId,
                EscrowTransactionType.RELEASE,
                amount,
                beforeAmount,
                afterAmount,
                referenceId,
                referenceType,
                description,
                occurredAt,
                createdAt
        );
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("?먯뒪?щ줈 嫄곕옒 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
        return amount;
    }

    private static BigDecimal validateNonNegativeAmount(BigDecimal amount) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("?먯뒪?щ줈 嫄곕옒 湲덉븸? ?뚯닔?????놁뒿?덈떎.");
        }
        return amount;
    }
}
