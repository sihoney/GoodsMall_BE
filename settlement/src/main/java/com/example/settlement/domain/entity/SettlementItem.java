package com.example.settlement.domain.entity;

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
@Table(name = "settlement_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItem {

    @Id
    @Column(name = "settlement_item_id", updatable = false)
    private UUID settlementItemId;

    @Column(name = "settlement_id")
    private UUID settlementId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "escrow_id", nullable = false)
    private UUID escrowId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "gross_amount", nullable = false)
    private Long grossAmount;

    @Column(name = "fee_amount", nullable = false)
    private Long feeAmount;

    @Column(name = "net_amount", nullable = false)
    private Long netAmount;

    @Column(name = "released_at", nullable = false)
    private LocalDateTime releasedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private SettlementItem(
            UUID settlementItemId,
            UUID settlementId,
            UUID orderId,
            UUID escrowId,
            UUID sellerId,
            Long grossAmount,
            Long feeAmount,
            Long netAmount,
            LocalDateTime releasedAt,
            LocalDateTime createdAt
    ) {
        this.settlementItemId = Objects.requireNonNull(settlementItemId);
        this.settlementId = settlementId;
        this.orderId = Objects.requireNonNull(orderId);
        this.escrowId = Objects.requireNonNull(escrowId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.grossAmount = Objects.requireNonNull(grossAmount);
        this.feeAmount = Objects.requireNonNull(feeAmount);
        this.netAmount = Objects.requireNonNull(netAmount);
        this.releasedAt = Objects.requireNonNull(releasedAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static SettlementItem create(
            UUID settlementItemId,
            UUID settlementId,
            UUID orderId,
            UUID escrowId,
            UUID sellerId,
            Long grossAmount,
            Long feeAmount,
            Long netAmount,
            LocalDateTime releasedAt,
            LocalDateTime createdAt
    ) {
        return new SettlementItem(
                settlementItemId,
                settlementId,
                orderId,
                escrowId,
                sellerId,
                grossAmount,
                feeAmount,
                netAmount,
                releasedAt,
                createdAt
        );
    }

    public void assignSettlement(UUID settlementId) {
        this.settlementId = Objects.requireNonNull(settlementId);
    }
}
