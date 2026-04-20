package com.example.settlement.domain.entity;

import com.example.settlement.domain.enumtype.SettlementItemStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 정산 원천 항목 엔티티(entity)다.
 *
 * <p>payment 모듈에서 escrow(에스크로)가 release(해제)될 때 생성된다.
 * escrowId에 unique constraint(유니크 제약)를 적용해 DB 레벨에서 중복 적재를 방지한다.
 * settlementItemStatus가 UNASSIGNED이면 아직 어떤 정산에도 연결되지 않은 항목이다.
 */
@Getter
@Entity
@Table(name = "settlement_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItem {

    @Id
    @Column(name = "settlement_item_id", updatable = false)
    private UUID settlementItemId;

    /**
     * 집계된 정산서 ID다. null이면 아직 월 집계에 포함되지 않은 미집계 상태다.
     */
    @Column(name = "settlement_id")
    private UUID settlementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_item_status", nullable = false)
    private SettlementItemStatus settlementItemStatus;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    /**
     * 중복 적재 방지(dedup) 기준 키다. escrowId는 전체 시스템에서 유일하게 발급된다.
     * unique = true로 DB 레벨 dedup(중복 방지)를 보장한다.
     */
    @Column(name = "escrow_id", nullable = false, unique = true)
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
            SettlementItemStatus settlementItemStatus,
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
        this.settlementItemStatus = Objects.requireNonNull(settlementItemStatus);
        this.orderId = Objects.requireNonNull(orderId);
        this.escrowId = Objects.requireNonNull(escrowId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.grossAmount = Objects.requireNonNull(grossAmount);
        this.feeAmount = Objects.requireNonNull(feeAmount);
        this.netAmount = Objects.requireNonNull(netAmount);
        this.releasedAt = Objects.requireNonNull(releasedAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * 정산 원천 항목을 생성한다.
     * settlementId는 집계 전에는 null로 넘긴다.
     */
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
                settlementId == null ? SettlementItemStatus.UNASSIGNED : SettlementItemStatus.ASSIGNED,
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

    /**
     * 정산 연결이 완료되면 settlementId를 기록하고 ASSIGNED 상태로 변경한다.
     */
    public void assignSettlement(UUID settlementId) {
        this.settlementId = Objects.requireNonNull(settlementId);
        this.settlementItemStatus = SettlementItemStatus.ASSIGNED;
    }

    public void markProcessing() {
        if (settlementItemStatus != SettlementItemStatus.UNASSIGNED) {
            throw new IllegalArgumentException("Only unassigned settlement item can move to processing.");
        }
        this.settlementItemStatus = SettlementItemStatus.PROCESSING;
    }

    public void markUnassigned() {
        this.settlementItemStatus = SettlementItemStatus.UNASSIGNED;
    }

    /**
     * 이미 어떤 정산에 연결된 항목인지 확인한다.
     */
    public boolean isAlreadyAggregated() {
        return this.settlementItemStatus == SettlementItemStatus.ASSIGNED;
    }

    public boolean isUnassigned() {
        return this.settlementItemStatus == SettlementItemStatus.UNASSIGNED;
    }

    public void applyRefund(Long grossReduction, Long feeReduction, Long netReduction) {
        long validatedGrossReduction = requireNonNegative(grossReduction, "grossReduction");
        long validatedFeeReduction = requireNonNegative(feeReduction, "feeReduction");
        long validatedNetReduction = requireNonNegative(netReduction, "netReduction");

        if (validatedGrossReduction > grossAmount) {
            throw new IllegalArgumentException("grossReduction exceeds grossAmount.");
        }
        if (validatedFeeReduction > feeAmount) {
            throw new IllegalArgumentException("feeReduction exceeds feeAmount.");
        }
        if (validatedNetReduction > netAmount) {
            throw new IllegalArgumentException("netReduction exceeds netAmount.");
        }
        if (validatedGrossReduction != validatedFeeReduction + validatedNetReduction) {
            throw new IllegalArgumentException("grossReduction must equal feeReduction + netReduction.");
        }

        this.grossAmount -= validatedGrossReduction;
        this.feeAmount -= validatedFeeReduction;
        this.netAmount -= validatedNetReduction;
    }

    public boolean isDepleted() {
        return this.grossAmount == 0L && this.feeAmount == 0L && this.netAmount == 0L;
    }

    private long requireNonNegative(Long amount, String fieldName) {
        long value = Objects.requireNonNull(amount, fieldName + " must not be null.");
        if (value < 0L) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
        return value;
    }
}
