package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.domain.enumtype.OrderPaymentStatus;
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
@Table(name = "order_payment", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPayment {

    @Id
    @Column(name = "order_payment_id", nullable = false, updatable = false)
    private UUID orderPaymentId;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    @Column(name = "buyer_member_id", nullable = false, updatable = false)
    private UUID buyerMemberId;

    @Column(name = "total_amount", nullable = false, updatable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private OrderPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private OrderPaymentStatus paymentStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private OrderPayment(
            UUID orderPaymentId,
            UUID orderId,
            UUID buyerMemberId,
            BigDecimal totalAmount,
            OrderPaymentMethod paymentMethod,
            OrderPaymentStatus paymentStatus,
            LocalDateTime paidAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.orderPaymentId = Objects.requireNonNull(orderPaymentId);
        this.orderId = Objects.requireNonNull(orderId);
        this.buyerMemberId = Objects.requireNonNull(buyerMemberId);
        this.totalAmount = validatePositiveAmount(totalAmount);
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.paymentStatus = Objects.requireNonNull(paymentStatus);
        this.paidAt = paidAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static OrderPayment createSucceeded(
            UUID orderPaymentId,
            UUID orderId,
            UUID buyerMemberId,
            BigDecimal totalAmount,
            OrderPaymentMethod paymentMethod,
            LocalDateTime paidAt
    ) {
        LocalDateTime now = Objects.requireNonNull(paidAt);
        return new OrderPayment(
                orderPaymentId,
                orderId,
                buyerMemberId,
                totalAmount,
                paymentMethod,
                OrderPaymentStatus.SUCCEEDED,
                now,
                now,
                now
        );
    }

    public void markRefundStatusByTotalRefundedAmount(BigDecimal totalRefundedAmount, LocalDateTime updatedAt) {
        BigDecimal validatedRefundedAmount = validateNonNegativeAmount(totalRefundedAmount);
        if (validatedRefundedAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("total refunded amount exceeds total payment amount.");
        }

        if (validatedRefundedAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = OrderPaymentStatus.SUCCEEDED;
        } else if (validatedRefundedAmount.compareTo(totalAmount) < 0) {
            this.paymentStatus = OrderPaymentStatus.PARTIAL_REFUNDED;
        } else {
            this.paymentStatus = OrderPaymentStatus.REFUNDED;
        }
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalAmount must be positive.");
        }
        return amount;
    }

    private static BigDecimal validateNonNegativeAmount(BigDecimal amount) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("total refunded amount must not be negative.");
        }
        return amount;
    }
}

