package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
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
@Table(name = "charge", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Charge {

    @Id
    @Column(name = "charge_id", nullable = false, updatable = false)
    private UUID chargeId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "wallet_id")
    private UUID walletId;

    @Column(name = "requested_amount", nullable = false)
    private Long requestedAmount;

    @Column(name = "approved_amount")
    private Long approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false)
    private PgProvider pgProvider;

    @Column(name = "pg_order_id", nullable = false, unique = true, length = 100)
    private String pgOrderId;

    @Column(name = "pg_payment_key", length = 200)
    private String pgPaymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status", nullable = false)
    private ChargeStatus chargeStatus;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    private Charge(
            UUID chargeId,
            UUID memberId,
            UUID walletId,
            Long requestedAmount,
            Long approvedAmount,
            PgProvider pgProvider,
            String pgOrderId,
            String pgPaymentKey,
            ChargeStatus chargeStatus,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            LocalDateTime failedAt,
            String failureReason
    ) {
        this.chargeId = Objects.requireNonNull(chargeId);
        this.memberId = Objects.requireNonNull(memberId);
        this.walletId = walletId;
        this.requestedAmount = Objects.requireNonNull(requestedAmount);
        this.approvedAmount = approvedAmount;
        this.pgProvider = Objects.requireNonNull(pgProvider);
        this.pgOrderId = Objects.requireNonNull(pgOrderId);
        this.pgPaymentKey = pgPaymentKey;
        this.chargeStatus = Objects.requireNonNull(chargeStatus);
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.approvedAt = approvedAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
    }

    public static Charge create(
            UUID chargeId,
            UUID memberId,
            UUID walletId,
            Long requestedAmount,
            PgProvider pgProvider,
            String pgOrderId,
            LocalDateTime requestedAt
    ) {
        return new Charge(
                chargeId,
                memberId,
                walletId,
                requestedAmount,
                null,
                pgProvider,
                pgOrderId,
                null,
                ChargeStatus.PENDING,
                requestedAt,
                null,
                null,
                null
        );
    }

    public void approve(Long approvedAmount, String pgPaymentKey, LocalDateTime approvedAt) {
        validatePendingStatus();
        this.approvedAmount = Objects.requireNonNull(approvedAmount);
        this.pgPaymentKey = Objects.requireNonNull(pgPaymentKey);
        this.approvedAt = Objects.requireNonNull(approvedAt);
        this.failedAt = null;
        this.failureReason = null;
        this.chargeStatus = ChargeStatus.SUCCESS;
    }

    public void fail(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.FAILED;
    }

    public void cancel() {
        validatePendingStatus();
        this.chargeStatus = ChargeStatus.CANCELLED;
    }

    public boolean isPending() {
        return chargeStatus == ChargeStatus.PENDING;
    }

    public boolean isSuccess() {
        return chargeStatus == ChargeStatus.SUCCESS;
    }

    private void validatePendingStatus() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending charges can be changed.");
        }
    }
}
