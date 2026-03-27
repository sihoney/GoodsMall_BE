package com.example.settlement.domain.entity;

import com.example.settlement.domain.enumtype.SettlementStatus;
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
@Table(name = "settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @Column(name = "settlement_id", updatable = false)
    private UUID settlementId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "settlement_year", nullable = false)
    private Integer settlementYear;

    @Column(name = "settlement_month", nullable = false)
    private Integer settlementMonth;

    @Column(name = "total_sales_amount", nullable = false)
    private Long totalSalesAmount;

    @Column(name = "fee_amount", nullable = false)
    private Long feeAmount;

    @Column(name = "final_settlement_amount", nullable = false)
    private Long finalSettlementAmount;

    @Column(name = "settled_amount", nullable = false)
    private Long settledAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Settlement(
            UUID settlementId,
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            Long totalSalesAmount,
            Long feeAmount,
            Long finalSettlementAmount,
            Long settledAmount,
            SettlementStatus settlementStatus,
            LocalDateTime settledAt,
            LocalDateTime requestedAt,
            LocalDateTime updatedAt
    ) {
        this.settlementId = Objects.requireNonNull(settlementId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.settlementYear = Objects.requireNonNull(settlementYear);
        this.settlementMonth = Objects.requireNonNull(settlementMonth);
        this.totalSalesAmount = Objects.requireNonNull(totalSalesAmount);
        this.feeAmount = Objects.requireNonNull(feeAmount);
        this.finalSettlementAmount = Objects.requireNonNull(finalSettlementAmount);
        this.settledAmount = Objects.requireNonNull(settledAmount);
        this.settlementStatus = Objects.requireNonNull(settlementStatus);
        this.settledAt = settledAt;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Settlement create(
            UUID settlementId,
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            Long totalSalesAmount,
            Long feeAmount,
            Long finalSettlementAmount,
            Long settledAmount,
            SettlementStatus settlementStatus,
            LocalDateTime settledAt,
            LocalDateTime requestedAt,
            LocalDateTime updatedAt
    ) {
        return new Settlement(
                settlementId,
                sellerId,
                settlementYear,
                settlementMonth,
                totalSalesAmount,
                feeAmount,
                finalSettlementAmount,
                settledAmount,
                settlementStatus,
                settledAt,
                requestedAt,
                updatedAt
        );
    }

    public static Settlement createPending(
            UUID settlementId,
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            Long totalSalesAmount,
            Long feeAmount,
            Long finalSettlementAmount,
            LocalDateTime requestedAt
    ) {
        LocalDateTime now = Objects.requireNonNull(requestedAt);
        return new Settlement(
                settlementId,
                sellerId,
                settlementYear,
                settlementMonth,
                validateNonNegative(totalSalesAmount, "totalSalesAmount"),
                validateNonNegative(feeAmount, "feeAmount"),
                validateNonNegative(finalSettlementAmount, "finalSettlementAmount"),
                0L,
                SettlementStatus.PENDING,
                null,
                now,
                now
        );
    }

    public void accumulate(
            Long salesAmount,
            Long feeAmount,
            Long finalSettlementAmount,
            LocalDateTime updatedAt
    ) {
        this.totalSalesAmount += validateNonNegative(salesAmount, "salesAmount");
        this.feeAmount += validateNonNegative(feeAmount, "feeAmount");
        this.finalSettlementAmount += validateNonNegative(finalSettlementAmount, "finalSettlementAmount");
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void complete(Long settledAmount, LocalDateTime settledAt, LocalDateTime updatedAt) {
        this.settledAmount = Objects.requireNonNull(settledAmount);
        this.settledAt = Objects.requireNonNull(settledAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.COMPLETED;
    }

    public void fail(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.FAILED;
    }

    private static Long validateNonNegative(Long amount, String fieldName) {
        if (Objects.requireNonNull(amount) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
        return amount;
    }
}
