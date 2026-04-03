package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.EscrowStatus;
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
@Table(name = "escrow", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 주문 결제 금액을 임시 보관하는 escrow aggregate다.
 * releaseAt 예약과 실제 release/refund 상태 전이를 한 엔티티에서 보장한다.
 */
public class Escrow {

    @Id
    @Column(name = "escrow_id", nullable = false, updatable = false)
    private UUID escrowId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "buyer_member_id", nullable = false)
    private UUID buyerMemberId;

    @Column(name = "seller_member_id", nullable = false)
    private UUID sellerMemberId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false)
    private EscrowStatus escrowStatus;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "release_at")
    private LocalDateTime releaseAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Escrow(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            Long amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.escrowId = Objects.requireNonNull(escrowId);
        this.orderId = Objects.requireNonNull(orderId);
        this.buyerMemberId = Objects.requireNonNull(buyerMemberId);
        this.sellerMemberId = Objects.requireNonNull(sellerMemberId);
        this.amount = Objects.requireNonNull(amount);
        this.escrowStatus = Objects.requireNonNull(escrowStatus);
        this.refundedAt = refundedAt;
        this.releasedAt = releasedAt;
        this.releaseAt = releaseAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt;
    }

    public static Escrow createHeld(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            Long amount,
            LocalDateTime releaseAt,
            LocalDateTime createdAt
    ) {
        LocalDateTime now = Objects.requireNonNull(createdAt);

        return new Escrow(
                escrowId,
                orderId,
                buyerMemberId,
                sellerMemberId,
                validateAmount(amount),
                EscrowStatus.HELD,
                null,
                null,
                releaseAt,
                now,
                now
        );
    }

    public static Escrow create(
            UUID escrowId,
            UUID orderId,
            UUID buyerMemberId,
            UUID sellerMemberId,
            Long amount,
            EscrowStatus escrowStatus,
            LocalDateTime refundedAt,
            LocalDateTime releasedAt,
            LocalDateTime releaseAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Escrow(
                escrowId,
                orderId,
                buyerMemberId,
                sellerMemberId,
                validateAmount(amount),
                escrowStatus,
                refundedAt,
                releasedAt,
                releaseAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * escrow 정산 완료 상태로 전이한다.
     * release는 HELD 상태에서만 허용되고, 중복 호출 방어는 엔티티가 마지막으로 확인한다.
     */
    public void release(LocalDateTime releasedAt, LocalDateTime updatedAt) {
        validateHeldStatus();
        this.escrowStatus = EscrowStatus.RELEASED;
        this.releasedAt = Objects.requireNonNull(releasedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * escrow 환불 완료 상태로 전이한다.
     * release와 동일하게 HELD 상태에서만 허용해 상충되는 종료 상태를 막는다.
     */
    public void refund(LocalDateTime refundedAt, LocalDateTime updatedAt) {
        validateHeldStatus();
        this.escrowStatus = EscrowStatus.REFUNDED;
        this.refundedAt = Objects.requireNonNull(refundedAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public boolean isHeld() {
        return escrowStatus == EscrowStatus.HELD;
    }

    public boolean isReleased() {
        return escrowStatus == EscrowStatus.RELEASED;
    }

    public boolean isRefunded() {
        return escrowStatus == EscrowStatus.REFUNDED;
    }

    public boolean isReleaseScheduled() {
        return releaseAt != null;
    }

    /**
     * 배송완료 이후 자동 구매확정 시점을 예약한다.
     * application에서 선분기하더라도 엔티티는 마지막 방어선으로 상태를 다시 확인한다.
     */
    public void scheduleReleaseAt(LocalDateTime releaseAt, LocalDateTime updatedAt) {
        if (!isHeld()) {
            throw new IllegalStateException("Only held escrow can be scheduled.");
        }
        if (this.releaseAt != null) {
            throw new IllegalStateException("Release time is already scheduled.");
        }
        this.releaseAt = Objects.requireNonNull(releaseAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    private void validateHeldStatus() {
        if (!isHeld()) {
            throw new IllegalStateException("Only held escrow can be changed.");
        }
    }

    private static Long validateAmount(Long amount) {
        Objects.requireNonNull(amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("Escrow amount must be positive.");
        }
        return amount;
    }
}
