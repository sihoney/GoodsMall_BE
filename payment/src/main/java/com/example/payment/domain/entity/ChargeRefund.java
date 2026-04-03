package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeRefundStatus;
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
@Table(name = "charge_refund", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 충전 환불 시도와 결과를 기록하는 엔티티다.
 * 성공 환불과 실패 환불 이력을 분리해서 남겨 재시도 및 추적에 사용한다.
 */
public class ChargeRefund {

    @Id
    @Column(name = "charge_refund_id", nullable = false, updatable = false)
    private UUID chargeRefundId;

    @Column(name = "charge_id", nullable = false)
    private UUID chargeId;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(name = "refund_reason", nullable = false, length = 255)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 30)
    private ChargeRefundStatus refundStatus;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    private ChargeRefund(
            UUID chargeRefundId,
            UUID chargeId,
            Long refundAmount,
            String refundReason,
            ChargeRefundStatus refundStatus,
            LocalDateTime requestedAt,
            LocalDateTime refundedAt,
            LocalDateTime failedAt,
            String failureReason
    ) {
        this.chargeRefundId = Objects.requireNonNull(chargeRefundId);
        this.chargeId = Objects.requireNonNull(chargeId);
        this.refundAmount = Objects.requireNonNull(refundAmount);
        this.refundReason = Objects.requireNonNull(refundReason);
        this.refundStatus = Objects.requireNonNull(refundStatus);
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.refundedAt = refundedAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
    }

    /**
     * PG 취소 성공으로 완료된 환불 이력을 생성한다.
     */
    public static ChargeRefund refunded(
            UUID chargeRefundId,
            UUID chargeId,
            Long refundAmount,
            String refundReason,
            LocalDateTime requestedAt,
            LocalDateTime refundedAt
    ) {
        return new ChargeRefund(
                chargeRefundId,
                chargeId,
                refundAmount,
                refundReason,
                ChargeRefundStatus.REFUNDED,
                requestedAt,
                refundedAt,
                null,
                null
        );
    }

    /**
     * PG 취소 실패로 남는 환불 실패 이력을 생성한다.
     */
    public static ChargeRefund failed(
            UUID chargeRefundId,
            UUID chargeId,
            Long refundAmount,
            String refundReason,
            LocalDateTime requestedAt,
            LocalDateTime failedAt,
            String failureReason
    ) {
        return new ChargeRefund(
                chargeRefundId,
                chargeId,
                refundAmount,
                refundReason,
                ChargeRefundStatus.FAILED,
                requestedAt,
                null,
                failedAt,
                Objects.requireNonNull(failureReason)
        );
    }
}
