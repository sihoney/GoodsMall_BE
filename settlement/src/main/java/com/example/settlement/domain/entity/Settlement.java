package com.example.settlement.domain.entity;

import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
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
//변경감지
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

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_type", nullable = false)
    private SettlementType settlementType;

    @Column(name = "settlement_year", nullable = false)
    private Integer settlementYear;

    @Column(name = "settlement_month", nullable = false)
    private Integer settlementMonth;

    @Column(name = "total_sales_amount", nullable = false)
    private BigDecimal totalSalesAmount;

    @Column(name = "fee_amount", nullable = false)
    private BigDecimal feeAmount;

    @Column(name = "final_settlement_amount", nullable = false)
    private BigDecimal finalSettlementAmount;

    @Column(name = "settled_amount", nullable = false)
    private BigDecimal settledAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "last_failure_reason")
    private String lastFailureReason;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Settlement(
            UUID settlementId,
            UUID sellerId,
            SettlementType settlementType,
            Integer settlementYear,
            Integer settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            BigDecimal settledAmount,
            SettlementStatus settlementStatus,
            LocalDateTime settledAt,
            String lastFailureReason,
            LocalDateTime requestedAt,
            LocalDateTime updatedAt
    ) {
        this.settlementId = Objects.requireNonNull(settlementId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.settlementType = Objects.requireNonNull(settlementType);
        this.settlementYear = Objects.requireNonNull(settlementYear);
        this.settlementMonth = Objects.requireNonNull(settlementMonth);
        this.totalSalesAmount = Objects.requireNonNull(totalSalesAmount);
        this.feeAmount = Objects.requireNonNull(feeAmount);
        this.finalSettlementAmount = Objects.requireNonNull(finalSettlementAmount);
        this.settledAmount = Objects.requireNonNull(settledAmount);
        this.settlementStatus = Objects.requireNonNull(settlementStatus);
        this.settledAt = settledAt;
        this.lastFailureReason = lastFailureReason;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Settlement create(
            UUID settlementId,
            UUID sellerId,
            SettlementType settlementType,
            Integer settlementYear,
            Integer settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            BigDecimal settledAmount,
            SettlementStatus settlementStatus,
            LocalDateTime settledAt,
            String lastFailureReason,
            LocalDateTime requestedAt,
            LocalDateTime updatedAt
    ) {
        return new Settlement(
                settlementId,
                sellerId,
                settlementType,
                settlementYear,
                settlementMonth,
                totalSalesAmount,
                feeAmount,
                finalSettlementAmount,
                settledAmount,
                settlementStatus,
                settledAt,
                lastFailureReason,
                requestedAt,
                updatedAt
        );
    }

    public static Settlement createMonthlyPending(
            UUID settlementId,
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            LocalDateTime requestedAt
    ) {
        LocalDateTime now = Objects.requireNonNull(requestedAt);
        return new Settlement(
                settlementId,
                sellerId,
                SettlementType.MONTHLY,
                settlementYear,
                settlementMonth,
                validateNonNegative(totalSalesAmount, "totalSalesAmount"),
                validateNonNegative(feeAmount, "feeAmount"),
                validateNonNegative(finalSettlementAmount, "finalSettlementAmount"),
                BigDecimal.ZERO,
                SettlementStatus.PENDING,
                null,
                null,
                now,
                now
        );
    }

    public static Settlement createPartialPending(
            UUID settlementId,
            UUID sellerId,
            Integer settlementYear,
            Integer settlementMonth,
            BigDecimal totalSalesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            LocalDateTime requestedAt
    ) {
        LocalDateTime now = Objects.requireNonNull(requestedAt);
        return new Settlement(
                settlementId,
                sellerId,
                SettlementType.PARTIAL,
                settlementYear,
                settlementMonth,
                validateNonNegative(totalSalesAmount, "totalSalesAmount"),
                validateNonNegative(feeAmount, "feeAmount"),
                validateNonNegative(finalSettlementAmount, "finalSettlementAmount"),
                BigDecimal.ZERO,
                SettlementStatus.PENDING,
                null,
                null,
                now,
                now
        );
    }

    public void accumulate(
            BigDecimal salesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            LocalDateTime updatedAt
    ) {
        this.totalSalesAmount = this.totalSalesAmount.add(validateNonNegative(salesAmount, "salesAmount"));
        this.feeAmount = this.feeAmount.add(validateNonNegative(feeAmount, "feeAmount"));
        this.finalSettlementAmount = this.finalSettlementAmount.add(validateNonNegative(finalSettlementAmount, "finalSettlementAmount"));
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void deduct(
            BigDecimal salesAmount,
            BigDecimal feeAmount,
            BigDecimal finalSettlementAmount,
            LocalDateTime updatedAt
    ) {
        BigDecimal validatedSalesAmount = validateNonNegative(salesAmount, "salesAmount");
        BigDecimal validatedFeeAmount = validateNonNegative(feeAmount, "feeAmount");
        BigDecimal validatedFinalSettlementAmount = validateNonNegative(finalSettlementAmount, "finalSettlementAmount");

        if (validatedSalesAmount.compareTo(this.totalSalesAmount) > 0) {
            throw new IllegalArgumentException("salesAmount exceeds totalSalesAmount.");
        }
        if (validatedFeeAmount.compareTo(this.feeAmount) > 0) {
            throw new IllegalArgumentException("feeAmount exceeds feeAmount.");
        }
        if (validatedFinalSettlementAmount.compareTo(this.finalSettlementAmount) > 0) {
            throw new IllegalArgumentException("finalSettlementAmount exceeds finalSettlementAmount.");
        }

        this.totalSalesAmount = this.totalSalesAmount.subtract(validatedSalesAmount);
        this.feeAmount = this.feeAmount.subtract(validatedFeeAmount);
        this.finalSettlementAmount = this.finalSettlementAmount.subtract(validatedFinalSettlementAmount);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void complete(BigDecimal settledAmount, LocalDateTime settledAt, LocalDateTime updatedAt) {
        this.settledAmount = Objects.requireNonNull(settledAmount);
        this.settledAt = Objects.requireNonNull(settledAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.COMPLETED;
    }

    public void markPayoutRequested(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.PROCESSING;
    }

    public void fail(String failureReason, LocalDateTime updatedAt) {
        this.lastFailureReason = failureReason;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.FAILED;
    }

    /**
     * RETRYABLE 실패 정산건을 재지급 요청 대상으로 되돌린다.
     * <p>
     * 실패 사유를 초기화하고 상태를 PENDING으로 복귀시켜
     * settlement 오케스트레이션이 재요청을 발행할 수 있게 한다.
     */
    public void requeueForPayout(LocalDateTime updatedAt) {
        this.lastFailureReason = null;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.settlementStatus = SettlementStatus.PENDING;
    }

    private static BigDecimal validateNonNegative(BigDecimal amount, String fieldName) {
        if (Objects.requireNonNull(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
        return amount;
    }
}
