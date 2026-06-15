package com.example.payment.escrow.domain.entity;

import com.example.payment.escrow.domain.enumtype.EscrowReferenceType;
import com.example.payment.escrow.domain.enumtype.EscrowStatus;
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
@Table(name = "escrow", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 二쇰Ц 寃곗젣 湲덉븸???꾩떆 蹂닿??섎뒗 escrow aggregate??
 * referenceId/referenceType?쇰줈 ?몃? ?먯쿇 ??곸쓣 ?앸퀎?섍퀬, release/refund ?곹깭 ?꾩씠瑜?蹂댁옣?쒕떎.
 */
public class Escrow {

    @Id
    @Column(name = "escrow_id", nullable = false, updatable = false)
    private UUID escrowId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private EscrowReferenceType referenceType;

    @Column(name = "buyer_member_id", nullable = false)
    private UUID buyerMemberId;

    @Column(name = "seller_member_id", nullable = false)
    private UUID sellerMemberId;

    /**
     * ?꾩옱 ?뺤궛 ??곸쑝濡??⑥븘?덈뒗 ?붿븸?대떎.
     */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * 二쇰Ц 寃곗젣 ?쒖젏???먮낯 escrow 湲덉븸?대떎.
     */
    @Column(name = "original_amount", nullable = false)
    private BigDecimal originalAmount;

    /**
     * ?꾩쟻 ?섎텋 湲덉븸?대떎.
     */
    @Column(name = "refunded_amount", nullable = false)
    private BigDecimal refundedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false)
    private EscrowStatus escrowStatus;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Escrow(
            UUID escrowId,
            UUID orderId,
            UUID referenceId,
            EscrowReferenceType referenceType,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            BigDecimal originalAmount,
            BigDecimal refundedAmount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.escrowId = Objects.requireNonNull(escrowId);
        this.orderId = Objects.requireNonNull(orderId);
        this.referenceId = Objects.requireNonNull(referenceId);
        this.referenceType = Objects.requireNonNull(referenceType);
        this.buyerMemberId = Objects.requireNonNull(buyerMemberId);
        this.sellerMemberId = Objects.requireNonNull(sellerMemberId);
        this.amount = Objects.requireNonNull(amount);
        this.originalAmount = Objects.requireNonNull(originalAmount);
        this.refundedAmount = Objects.requireNonNull(refundedAmount);
        this.escrowStatus = Objects.requireNonNull(escrowStatus);
        this.refundedAt = refundedAt;
        this.releasedAt = releasedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt;
    }

    public static Escrow createHeld(
            UUID escrowId,
            UUID orderId,
            UUID referenceId,
            EscrowReferenceType referenceType,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {
        LocalDateTime now = Objects.requireNonNull(createdAt);
        BigDecimal validatedAmount = validatePositiveAmount(amount);
        return new Escrow(
                escrowId,
                orderId,
                referenceId,
                referenceType,
                buyerMemberId,
                sellerMemberId,
                validatedAmount,
                validatedAmount,
                BigDecimal.ZERO,
                EscrowStatus.HELD,
                null,
                null,
                now,
                now
        );
    }

    /**
     * ?섏쐞 ?명솚???⑺넗由щ떎. reference??ORDER 湲곗??쇰줈 梨꾩슫??
     */
    public static Escrow createHeld(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {
        return createHeld(
                escrowId,
                orderId,
                orderId,
                EscrowReferenceType.ORDER,
                buyerMemberId,
                sellerMemberId,
                amount,
                createdAt
        );
    }

    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            UUID referenceId,
            EscrowReferenceType referenceType,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        BigDecimal validatedAmount = validatePositiveAmount(amount);
        return new Escrow(
                escrowId,
                orderId,
                referenceId,
                referenceType,
                buyerMemberId,
                sellerMemberId,
                validatedAmount,
                validatedAmount,
                BigDecimal.ZERO,
                escrowStatus,
                refundedAt,
                releasedAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * ?섏쐞 ?명솚???⑺넗由щ떎. reference??ORDER 湲곗??쇰줈 梨꾩슫??
     */
    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return create(
                escrowId,
                orderId,
                orderId,
                EscrowReferenceType.ORDER,
                buyerMemberId,
                sellerMemberId,
                amount,
                escrowStatus,
                refundedAt,
                releasedAt,
                releaseAt,
                createdAt,
                updatedAt
        );
    }

    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            UUID referenceId,
            EscrowReferenceType referenceType,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            BigDecimal originalAmount,
            BigDecimal refundedAmount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        BigDecimal validatedAmount = validateNonNegativeAmount(amount);
        BigDecimal validatedOriginalAmount = validatePositiveAmount(originalAmount);
        BigDecimal validatedRefundedAmount = validateNonNegativeAmount(refundedAmount);

        if (validatedRefundedAmount.compareTo(validatedOriginalAmount) > 0) {
            throw new IllegalArgumentException("?섎텋 湲덉븸? ?먭툑蹂대떎 ?????놁뒿?덈떎.");
        }
        if (validatedAmount.compareTo(validatedOriginalAmount.subtract(validatedRefundedAmount)) != 0) {
            throw new IllegalArgumentException("?먯뒪?щ줈 湲덉븸? ?먭툑?먯꽌 ?섎텋 湲덉븸??類 媛믨낵 ?쇱튂?댁빞 ?⑸땲??");
        }
        return new Escrow(
                escrowId,
                orderId,
                referenceId,
                referenceType,
                buyerMemberId,
                sellerMemberId,
                validatedAmount,
                validatedOriginalAmount,
                validatedRefundedAmount,
                escrowStatus,
                refundedAt,
                releasedAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * ?섏쐞 ?명솚???⑺넗由щ떎. reference??ORDER 湲곗??쇰줈 梨꾩슫??
     */
    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            BigDecimal amount,
            BigDecimal originalAmount,
            BigDecimal refundedAmount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return create(
                escrowId,
                orderId,
                orderId,
                EscrowReferenceType.ORDER,
                buyerMemberId,
                sellerMemberId,
                amount,
                originalAmount,
                refundedAmount,
                escrowStatus,
                refundedAt,
                releasedAt,
                releaseAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * escrow ?뺤궛 ?꾨즺 ?곹깭濡??꾩씠?쒕떎.
     * release??HELD ?곹깭?먯꽌留??덉슜?섍퀬, 以묐났 ?몄텧 諛⑹뼱???뷀떚?곌? 留덉?留됱쑝濡??뺤씤?쒕떎.
     */
    public void release(LocalDateTime releasedAt, LocalDateTime updatedAt) {
        validateHeldStatus();
        this.escrowStatus = EscrowStatus.RELEASED;
        this.releasedAt = Objects.requireNonNull(releasedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * escrow ?섎텋 ?꾨즺 ?곹깭濡??꾩씠?쒕떎.
     * release? ?숈씪?섍쾶 HELD ?곹깭?먯꽌留??덉슜?섍퀬, 醫낅떒 ?곹깭??醫낅즺 ?곹깭瑜?留됰뒗??
     */
    public void refund(LocalDateTime refundedAt, LocalDateTime updatedAt) {
        validateHeldStatus();
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("?붿븸??0???먯뒪?щ줈留??섎텋 ?꾨즺濡??쒖떆?????덉뒿?덈떎.");
        }
        this.escrowStatus = EscrowStatus.REFUNDED;
        this.refundedAt = Objects.requireNonNull(refundedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * HELD/RELEASED escrow?먯꽌 ?섎텋 湲덉븸留뚰겮 ?뺤궛????붿븸(amount)??李④컧?쒕떎.
     * amount媛 0???섎㈃ REFUNDED ?곹깭濡??꾩씠?쒕떎.
     */
    public void applyRefundAmount(BigDecimal refundAmount, LocalDateTime refundedAt, LocalDateTime updatedAt) {
        validateRefundableStatus();
        BigDecimal validatedRefundAmount = validatePositiveAmount(refundAmount);
        if (validatedRefundAmount.compareTo(amount) > 0) {
            throw new IllegalArgumentException("?섎텋 湲덉븸???먯뒪?щ줈 湲덉븸??珥덇낵?⑸땲??");
        }
        this.amount = this.amount.subtract(validatedRefundAmount);
        this.refundedAmount = this.refundedAmount.add(validatedRefundAmount);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (this.amount.compareTo(BigDecimal.ZERO) == 0) {
            this.escrowStatus = EscrowStatus.REFUNDED;
            this.refundedAt = Objects.requireNonNull(refundedAt);
        }
    }

    public boolean isHeld() { return escrowStatus == EscrowStatus.HELD; }
    public boolean isReleased() { return escrowStatus == EscrowStatus.RELEASED; }
    public boolean isRefunded() { return escrowStatus == EscrowStatus.REFUNDED; }
    public boolean isOrderItemReference() { return referenceType == EscrowReferenceType.ORDER_ITEM; }

    private void validateHeldStatus() {
        if (!isHeld()) throw new IllegalStateException("蹂닿? ?곹깭???먯뒪?щ줈留?蹂寃쏀븷 ???덉뒿?덈떎.");
    }

    private void validateRefundableStatus() {
        if (!isHeld() && !isReleased()) throw new IllegalStateException("蹂닿? ?먮뒗 ?뺤궛 ?꾨즺 ?곹깭???먯뒪?щ줈留??섎텋?????덉뒿?덈떎.");
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("?먯뒪?щ줈 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        return amount;
    }

    private static BigDecimal validateNonNegativeAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("?먯뒪?щ줈 湲덉븸? ?뚯닔?????놁뒿?덈떎.");
        return amount;
    }
}
