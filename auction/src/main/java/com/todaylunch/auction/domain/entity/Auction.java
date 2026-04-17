package com.todaylunch.auction.domain.entity;

import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "auction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction {

    private static final long EXTEND_THRESHOLD_SECONDS = 30L;
    private static final long EXTEND_AMOUNT_SECONDS = 5L;

    @Id
    @Column(name = "auction_id", nullable = false, updatable = false)
    private UUID auctionId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "current_highest_price", precision = 19, scale = 2)
    private BigDecimal currentHighestPrice;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "close_at", nullable = false)
    private LocalDateTime closeAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AuctionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Auction(
            UUID auctionId,
            UUID productId,
            UUID sellerId,
            BigDecimal startPrice,
            Integer durationMinutes,
            LocalDateTime startedAt,
            LocalDateTime closeAt,
            AuctionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.auctionId = Objects.requireNonNull(auctionId);
        this.productId = Objects.requireNonNull(productId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.startPrice = Objects.requireNonNull(startPrice);
        this.durationMinutes = Objects.requireNonNull(durationMinutes);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.closeAt = Objects.requireNonNull(closeAt);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.currentHighestPrice = null;
    }

    public static Auction create(
            UUID productId,
            UUID sellerId,
            BigDecimal startPrice,
            Integer durationMinutes,
            LocalDateTime startedAt
    ) {
        validateStartPrice(startPrice);
        validateDuration(durationMinutes);

        LocalDateTime now = LocalDateTime.now();
        return new Auction(
                UUID.randomUUID(),
                productId,
                sellerId,
                startPrice,
                durationMinutes,
                startedAt,
                startedAt.plusMinutes(durationMinutes),
                AuctionStatus.WAITING,
                now,
                now
        );
    }

    public void start() {
        if (this.status != AuctionStatus.WAITING) {
            throw new IllegalStateException("대기 중인 경매만 시작할 수 있습니다");
        }
        this.status = AuctionStatus.ONGOING;
        this.updatedAt = LocalDateTime.now();
    }


    public void applyConfirmedBid(BigDecimal bidPrice, LocalDateTime now) {
        if (this.status != AuctionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 경매가 아닙니다");
        }
        if (!isHigherThanCurrent(bidPrice)) {
            throw new IllegalArgumentException("현재 최고가보다 높은 금액이어야 합니다");
        }
        this.currentHighestPrice = bidPrice;
        extendTimeIfNearEnd(now);
        this.updatedAt = now;
    }

    public void changeToPendingPayment() {
        if (this.status != AuctionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 경매만 결제 대기로 전환할 수 있습니다");
        }
        this.status = AuctionStatus.PENDING_PAYMENT;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        if (this.status != AuctionStatus.ONGOING && this.status != AuctionStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("진행 중이거나 결제 대기 상태의 경매만 유찰 처리할 수 있습니다");
        }
        this.status = AuctionStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /** 결제 완료 시 낙찰 확정. PENDING_PAYMENT에서만 허용되는 비가역 전이. */
    public void complete() {
        if (this.status != AuctionStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태의 경매만 낙찰 확정할 수 있습니다");
        }
        this.status = AuctionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isHigherThanCurrent(BigDecimal bidPrice) {
        BigDecimal reference = this.currentHighestPrice != null ? this.currentHighestPrice : this.startPrice;
        return bidPrice.compareTo(reference) > 0;
    }

    private void extendTimeIfNearEnd(LocalDateTime now) {
        long remaining = Duration.between(now, this.closeAt).toSeconds();
        if (remaining > 0 && remaining <= EXTEND_THRESHOLD_SECONDS) {
            this.closeAt = this.closeAt.plusSeconds(EXTEND_AMOUNT_SECONDS);
        }
    }

    private static void validateStartPrice(BigDecimal startPrice) {
        if (startPrice == null || startPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("시작가는 0보다 커야 합니다");
        }
    }

    private static void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("경매 시간은 0분보다 커야 합니다");
        }
    }
}
