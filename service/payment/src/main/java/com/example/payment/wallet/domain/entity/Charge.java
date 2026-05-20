package com.example.payment.wallet.domain.entity;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
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
//蹂寃쎄컧吏
@Getter
@Entity
@Table(name = "charge", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 異⑹쟾 ?붿껌怨??뺤씤 寃곌낵瑜??쒗쁽?섎뒗 charge aggregate??
 * ?붿껌 ?앹꽦 ?댄썑 PENDING, SUCCESS, FAILED, CANCELLED ?곹깭 ?꾩씠瑜??ㅼ뒪濡?蹂댁옣?쒕떎.
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
     * ?꾩껜 ?꾨뱶 ?앹꽦??(?뚯뒪?몃굹 ?꾩닔 ?곹솴?먯꽌 ?ъ슜)
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
     * PG ?뺤씤 ?꾨즺 ?뺣낫瑜?諛섏쁺?섍퀬 charge瑜?SUCCESS ?곹깭濡??댄뻾?쒕떎.
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

    // 異⑹쟾 ?ㅽ뙣 由щ떎?대젆???댁쑀瑜?湲곕줉?섍퀬 charge瑜?REDIRECT_FAILED ?곹깭濡??댄뻾?쒕떎.
    public void failAtRedirect(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.REDIRECT_FAILED;
    }

    /**
     * 異⑹쟾 ?뺤씤 ?ㅽ뙣 ?댁쑀瑜?湲곕줉?섍퀬 charge瑜?FAILED ?곹깭濡??댄뻾?쒕떎.
     */
    public void fail(String failureReason, LocalDateTime failedAt) {
        validatePendingStatus();
        this.failedAt = Objects.requireNonNull(failedAt);
        this.failureReason = Objects.requireNonNull(failureReason);
        this.chargeStatus = ChargeStatus.CONFIRM_FAILED;
    }

    /**
     * ?꾩쭅 ?뺤씤?섏? ?딆? charge瑜?痍⑥냼 ?곹깭濡??댄뻾?쒕떎.
     */
    public void cancel() {
        validatePendingStatus();
        this.chargeStatus = ChargeStatus.CANCELLED;
    }

    // ?꾩옱 異⑹쟾 ?곹깭媛 ?湲곗쨷?몄? ?뺤씤?쒕떎.
    public boolean isPending() {
        return chargeStatus == ChargeStatus.PENDING;
    }


    // ?꾩옱 異⑹쟾 ?곹깭媛 由щ떎?대젆???ㅽ뙣?몄? ?뺤씤?쒕떎.
    public boolean isRedirectFailed() {
        return chargeStatus == ChargeStatus.REDIRECT_FAILED;
    }


    // 異⑹쟾 ?곹깭媛 ?뺤씤 ?꾨즺嫄곕굹 由щ떎?대젆???ㅽ뙣?몄? ?뺤씤?쒕떎.
    // ?꾩옱 異⑹쟾 ?곹깭媛 ?뺤씤 ?꾨즺?몄? ?뺤씤?쒕떎.
    public boolean isSuccess() {
        return chargeStatus == ChargeStatus.CONFIRM_SUCCESS;
    }

    /**
     * charge ?곹깭 蹂寃쎌? PENDING?먯꽌留??덉슜?쒕떎.
     * <p>
     * ?꾨찓??洹쒖튃? ?뷀떚???대??먯꽌
     * ?쒕퉬??怨꾩링??留뚮뱾 ???녿뒗 ?곹깭 ?댄뻾留뚯쓣 諛⑹??쒕떎.
     */
    private void validatePendingStatus() {
        if (!isPending()) {
            throw new IllegalStateException("?湲??곹깭??異⑹쟾留?蹂寃쏀븷 ???덉뒿?덈떎.");
        }
    }

}
