package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.OrderPaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_payment_allocation", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPaymentAllocation {

    @Id
    @Column(name = "allocation_id", nullable = false, updatable = false)
    private UUID allocationId;

    @Column(name = "order_payment_id", nullable = false, updatable = false)
    private UUID orderPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20, updatable = false)
    private OrderPaymentMethod method;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "card_transaction_group_id", updatable = false)
    private UUID cardTransactionGroupId;

    @Column(name = "wallet_transaction_id", updatable = false)
    private UUID walletTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private OrderPaymentAllocation(
            UUID allocationId,
            UUID orderPaymentId,
            OrderPaymentMethod method,
            BigDecimal amount,
            UUID cardTransactionGroupId,
            UUID walletTransactionId,
            LocalDateTime createdAt
    ) {
        this.allocationId = Objects.requireNonNull(allocationId);
        this.orderPaymentId = Objects.requireNonNull(orderPaymentId);
        this.method = Objects.requireNonNull(method);
        this.amount = validatePositiveAmount(amount);
        this.cardTransactionGroupId = cardTransactionGroupId;
        this.walletTransactionId = walletTransactionId;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static OrderPaymentAllocation walletAllocation(
            UUID allocationId,
            UUID orderPaymentId,
            BigDecimal amount,
            UUID walletTransactionId,
            LocalDateTime createdAt
    ) {
        return new OrderPaymentAllocation(
                allocationId,
                orderPaymentId,
                OrderPaymentMethod.WALLET,
                amount,
                null,
                Objects.requireNonNull(walletTransactionId),
                createdAt
        );
    }

    public static OrderPaymentAllocation cardAllocation(
            UUID allocationId,
            UUID orderPaymentId,
            BigDecimal amount,
            UUID cardTransactionGroupId,
            LocalDateTime createdAt
    ) {
        return new OrderPaymentAllocation(
                allocationId,
                orderPaymentId,
                OrderPaymentMethod.CARD,
                amount,
                Objects.requireNonNull(cardTransactionGroupId),
                null,
                createdAt
        );
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("allocation amount must be positive.");
        }
        return amount;
    }
}

