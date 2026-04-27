package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeStatus;
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
@Table(name = "charge", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 충전 요청과 확인 결과를 표현하는 charge aggregate다.
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
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

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
     * 전체 필드 생성자 (테스트나 필수 상황에서 사용)
     */
    private Charge(
            UUID chargeId,
            UUID memberId,
            UUID walletId,
            BigDecimal requestedAmount,
            BigDecimal approvedAmount,
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
            BigDecimal requestedAmount,
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
     * PG 확인 완료 정보를 반영하고 charge를 SUCCESS 상태로 이행한다.
     */
    public void approve(BigDecimal approvedAmount, String pgPaymentKey, LocalDateTime approvedAt, String tossBankCode) {
        validatePendingStatus();
        this.approvedAmount = Objects.requireNonNull(approvedAmount);
        this.pgPaymentKey = Objects.requireNonNull(pgPaymentKey);
        this.approvedAt = Objects.requireNonNull(approvedAt);
        this.tossBankCode = tossBankCode;
        this.failedAt = null;
        this.failureReason = null;
        this.chargeStatus = ChargeStatus.CONFIRM_SUCCESS;
    }

    // 충전 실패 리다이렉트 이유를 기록하고 charge를 REDIRECT_FAILED 상태로 이행한다.
    public void failAtRedirect(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.REDIRECT_FAILED;
    }

    /**
     * 충전 확인 실패 이유를 기록하고 charge를 FAILED 상태로 이행한다.
     */
    public void fail(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.CONFIRM_FAILED;
    }

    /**
     * 아직 확인하지 않은 charge를 취소 상태로 이행한다.
     */
    public void cancel() {
        validatePendingStatus();
        this.chargeStatus = ChargeStatus.CANCELLED;
    }

    // 현재 충전 상태가 대기중인지 확인한다.
    public boolean isPending() {
        return chargeStatus == ChargeStatus.PENDING;
    }


    // 현재 충전 상태가 리다이렉트 실패인지 확인한다.
    public boolean isRedirectFailed() {
        return chargeStatus == ChargeStatus.REDIRECT_FAILED;
    }


    // 충전 상태가 확인 완료거나 리다이렉트 실패인지 확인한다.
    // 현재 충전 상태가 확인 완료인지 확인한다.
    public boolean isSuccess() {
        return chargeStatus == ChargeStatus.CONFIRM_SUCCESS;
    }

    /**
     * charge 상태 변경은 PENDING에서만 허용된다.
     * <p>
     * 도메인 규칙은 엔티티 내부에서
     * 서비스 계층이 만들 수 없는 상태 이행만을 방지한다.
     */
    private void validatePendingStatus() {
        if (!isPending()) {
            throw new IllegalStateException("대기 상태의 충전만 변경할 수 있습니다.");
        }
    }

}
