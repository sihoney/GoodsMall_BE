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
@Table(name = "settlement_refund_manual_action")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementRefundManualAction {

    @Id
    @Column(name = "manual_action_id", updatable = false)
    private UUID manualActionId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "refund_id", nullable = false)
    private UUID refundId;

    @Column(name = "settlement_id", nullable = false)
    private UUID settlementId;

    @Column(name = "settlement_item_id", nullable = false)
    private UUID settlementItemId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "escrow_id", nullable = false)
    private UUID escrowId;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private SettlementRefundManualAction(
            UUID manualActionId,
            UUID eventId,
            UUID refundId,
            UUID settlementId,
            UUID settlementItemId,
            UUID orderId,
            UUID escrowId,
            UUID orderItemId,
            UUID sellerId,
            UUID buyerId,
            Long refundAmount,
            String reason,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        this.manualActionId = Objects.requireNonNull(manualActionId);
        this.eventId = Objects.requireNonNull(eventId);
        this.refundId = Objects.requireNonNull(refundId);
        this.settlementId = Objects.requireNonNull(settlementId);
        this.settlementItemId = Objects.requireNonNull(settlementItemId);
        this.orderId = Objects.requireNonNull(orderId);
        this.escrowId = Objects.requireNonNull(escrowId);
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.refundAmount = Objects.requireNonNull(refundAmount);
        this.reason = Objects.requireNonNull(reason);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static SettlementRefundManualAction create(
            UUID manualActionId,
            UUID eventId,
            UUID refundId,
            UUID settlementId,
            UUID settlementItemId,
            UUID orderId,
            UUID escrowId,
            UUID orderItemId,
            UUID sellerId,
            UUID buyerId,
            Long refundAmount,
            String reason,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
        return new SettlementRefundManualAction(
                manualActionId,
                eventId,
                refundId,
                settlementId,
                settlementItemId,
                orderId,
                escrowId,
                orderItemId,
                sellerId,
                buyerId,
                refundAmount,
                reason,
                occurredAt,
                createdAt
        );
    }
}
