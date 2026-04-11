package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeStatus;
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
/**
 * 충전 요청과 승인 결과를 표현하는 charge aggregate다.
 * 요청 생성 이후 PENDING, SUCCESS, FAILED, CANCELLED 상태 전이를 스스로 보장한다.
 */
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

    @Column(name = "toss_bank_code", length = 30)
    private String tossBankCode;

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

    /**
     * 전체 필드 생성자 (테스트 또는 특수 상황용)
     */
    // todo : 필요 없는 필드는 제거 예정
    private Charge(
            UUID chargeId,
            UUID memberId,
            UUID walletId,
            Long requestedAmount,
            Long approvedAmount,
            String tossBankCode,
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
        this.tossBankCode = tossBankCode;
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
            String pgOrderId,
            LocalDateTime requestedAt
    ) {
        return new Charge(
                chargeId,
                memberId,
                walletId,
                requestedAmount,
                null,
                null,
                pgOrderId,
                null,
                ChargeStatus.PENDING,
                requestedAt,
                null,
                null,
                null
        );
    }

    /**
     * PG 승인 완료 정보를 반영하고 charge를 SUCCESS 상태로 전이한다.
     */
    public void approve(Long approvedAmount, String pgPaymentKey, LocalDateTime approvedAt, String tossBankCode) {
        validatePendingStatus();
        this.approvedAmount = Objects.requireNonNull(approvedAmount);
        this.pgPaymentKey = Objects.requireNonNull(pgPaymentKey);
        this.approvedAt = Objects.requireNonNull(approvedAt);
        this.tossBankCode = tossBankCode;
        this.failedAt = null;
        this.failureReason = null;
        this.chargeStatus = ChargeStatus.CONFIRM_SUCCESS;
    }

    // 충전 실패 리다이렉트 사유를 기록하고 charge를 REDIRECT_FAILED 상태로 전이한다.
    public void failAtRedirect(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.REDIRECT_FAILED;
    }

    /**
     * 충전 승인 실패 사유를 기록하고 charge를 FAILED 상태로 전이한다.
     */
    public void fail(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.CONFIRM_FAILED;
    }

    /**
     * 아직 승인되지 않은 charge를 취소 상태로 전이한다.
     */
    public void cancel() {
        validatePendingStatus();
        this.chargeStatus = ChargeStatus.CANCELLED;
    }

    // 현재 충전 상태가 대기 중인지 확인한다.
    public boolean isPending() {
        return chargeStatus == ChargeStatus.PENDING;
    }


    // 현재 충전 상태가 리다이렉트 실패인지 확인한다.
    public boolean isRedirectFailed() {
        return chargeStatus == ChargeStatus.REDIRECT_FAILED;
    }


    // 충전 상태가 승인 대기 또는 리다이렉트 실패인지 확인한다.
    // 현재 충전 상태가 승인 완료인지 확인한다.
    public boolean isSuccess() {
        return chargeStatus == ChargeStatus.CONFIRM_SUCCESS;
    }

    /**
     * charge 상태 변경은 PENDING에서만 허용한다.
     * <p>
     * 도메인 규칙을 엔티티 내부에 둬서
     * 서비스 계층이 실수로 잘못된 상태 전이를 만들지 않도록 방지한다.
     */
    private void validatePendingStatus() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending charges can be changed.");
        }
    }

}
