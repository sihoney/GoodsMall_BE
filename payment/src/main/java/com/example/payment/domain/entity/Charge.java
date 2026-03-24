package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
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
@Table(name = "charge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Charge {

    @Id
    @Column(name = "charge_id", nullable = false, updatable = false)
    private UUID chargeId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false)
    private PgProvider pgProvider;

    @Column(name = "pg_transaction_id")
    private UUID pgTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status", nullable = false)
    private ChargeStatus chargeStatus;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    private Charge(
            UUID chargeId,
            UUID memberId,
            UUID transactionId,
            BigDecimal requestedAmount,
            BigDecimal approvedAmount,
            PgProvider pgProvider,
            UUID pgTransactionId,
            ChargeStatus chargeStatus,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt
    ) {
        this.chargeId = Objects.requireNonNull(chargeId);
        this.memberId = Objects.requireNonNull(memberId);
        this.transactionId = Objects.requireNonNull(transactionId);
        this.requestedAmount = Objects.requireNonNull(requestedAmount);
        this.approvedAmount = approvedAmount;
        this.pgProvider = Objects.requireNonNull(pgProvider);
        this.pgTransactionId = pgTransactionId;
        this.chargeStatus = Objects.requireNonNull(chargeStatus);
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.approvedAt = approvedAt;
    }

    public static Charge create(
            UUID chargeId,
            UUID memberId,
            UUID transactionId,
            BigDecimal requestedAmount,
            PgProvider pgProvider,
            ChargeStatus chargeStatus,
            LocalDateTime requestedAt
    ) {
        return new Charge(
                chargeId,
                memberId,
                transactionId,
                requestedAmount,
                null,
                pgProvider,
                null,
                chargeStatus,
                requestedAt,
                null
        );
    }

    public void approve(BigDecimal approvedAmount, UUID pgTransactionId, LocalDateTime approvedAt) {
        this.approvedAmount = Objects.requireNonNull(approvedAmount);
        this.pgTransactionId = Objects.requireNonNull(pgTransactionId);
        this.approvedAt = Objects.requireNonNull(approvedAt);
        this.chargeStatus = ChargeStatus.SUCCESS;
    }

    public void fail() {
        this.chargeStatus = ChargeStatus.FAILED;
    }

    public void cancel() {
        this.chargeStatus = ChargeStatus.CANCELLED;
    }
}
