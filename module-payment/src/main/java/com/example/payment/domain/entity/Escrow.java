package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.EscrowStatus;
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
@Table(name = "escrow")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Escrow {

    @Id
    @Column(name = "escrow_id", nullable = false, updatable = false)
    private UUID escrowId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false)
    private EscrowStatus escrowStatus;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "release_at")
    private LocalDateTime releaseAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Escrow(
            UUID escrowId,
            UUID orderId,
            BigDecimal amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.escrowId = Objects.requireNonNull(escrowId);
        this.orderId = Objects.requireNonNull(orderId);
        this.amount = Objects.requireNonNull(amount);
        this.escrowStatus = Objects.requireNonNull(escrowStatus);
        this.refundedAt = refundedAt;
        this.releasedAt = releasedAt;
        this.releaseAt = releaseAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt;
    }

    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            BigDecimal amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Escrow(
                escrowId,
                orderId,
                amount,
                escrowStatus,
                refundedAt,
                releasedAt,
                releaseAt,
                createdAt,
                updatedAt
        );
    }

    public void release(LocalDateTime releasedAt, LocalDateTime updatedAt) {
        this.escrowStatus = EscrowStatus.RELEASED;
        this.releasedAt = Objects.requireNonNull(releasedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void refund(LocalDateTime refundedAt, LocalDateTime updatedAt) {
        this.escrowStatus = EscrowStatus.REFUNDED;
        this.refundedAt = Objects.requireNonNull(refundedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
