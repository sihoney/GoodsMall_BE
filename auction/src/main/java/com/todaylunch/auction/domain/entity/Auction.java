package com.todaylunch.auction.domain.entity;

import com.todaylunch.auction.common.exception.domain.AuctionNotOngoingException;
import com.todaylunch.auction.common.exception.domain.BidIncrementNotMetException;
import com.todaylunch.auction.common.exception.domain.BidPriceUnitNotMetException;
import com.todaylunch.auction.common.exception.domain.SelfBidNotAllowedException;
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
    private static final BigDecimal BID_PRICE_UNIT = new BigDecimal("100");

    @Id
    @Column(name = "auction_id", nullable = false, updatable = false)
    private UUID auctionId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_title", nullable = false)
    private String productTitle;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "bid_unit", nullable = false, precision = 19, scale = 2)
    private BigDecimal bidUnit;

    @Column(name = "current_highest_price", precision = 19, scale = 2)
    private BigDecimal currentHighestPrice;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "scheduled_close_at", nullable = false, updatable = false)
    private LocalDateTime scheduledCloseAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

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
            String productTitle,
            UUID sellerId,
            BigDecimal startPrice,
            BigDecimal bidUnit,
            LocalDateTime startedAt,
            LocalDateTime scheduledCloseAt,
            LocalDateTime endedAt,
            AuctionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.auctionId = Objects.requireNonNull(auctionId);
        this.productId = Objects.requireNonNull(productId);
        this.productTitle = Objects.requireNonNull(productTitle);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.startPrice = Objects.requireNonNull(startPrice);
        this.bidUnit = Objects.requireNonNull(bidUnit);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.scheduledCloseAt = Objects.requireNonNull(scheduledCloseAt);
        this.endedAt = Objects.requireNonNull(endedAt);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.currentHighestPrice = null;
    }

    public static Auction create(
            UUID productId,
            String productTitle,
            UUID sellerId,
            BigDecimal startPrice,
            BigDecimal bidUnit,
            LocalDateTime startedAt,
            LocalDateTime scheduledCloseAt
    ) {
        validateStartPrice(startPrice);
        validateBidUnit(bidUnit);
        validateScheduledCloseAt(startedAt, scheduledCloseAt);

        LocalDateTime now = LocalDateTime.now();
        return new Auction(
                UUID.randomUUID(),
                productId,
                productTitle,
                sellerId,
                startPrice,
                bidUnit,
                startedAt,
                scheduledCloseAt,
                scheduledCloseAt,
                AuctionStatus.WAITING,
                now,
                now
        );
    }

    private static void validateStartPrice(BigDecimal startPrice) {
        if (startPrice == null || startPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("시작가는 0보다 커야 합니다");
        }
    }

    private static void validateBidUnit(BigDecimal bidUnit) {
        if (bidUnit == null || bidUnit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("최소 입찰 단위는 0보다 커야 합니다");
        }
    }

    private static void validateScheduledCloseAt(LocalDateTime startedAt, LocalDateTime scheduledCloseAt) {
        if (startedAt == null || scheduledCloseAt == null) {
            throw new IllegalArgumentException("시작/종료 시각이 필요합니다");
        }
        if (!scheduledCloseAt.isAfter(startedAt)) {
            throw new IllegalArgumentException("종료 시각은 시작 시각 이후여야 합니다");
        }
    }

    public void start() {
        if (this.status != AuctionStatus.WAITING) {
            throw new IllegalStateException("대기 중인 경매만 시작할 수 있습니다");
        }
        this.status = AuctionStatus.ONGOING;
        this.updatedAt = LocalDateTime.now();
    }

    public void rollbackHighestPrice(BigDecimal previousHighestPrice) {
        this.currentHighestPrice = previousHighestPrice;
    }

    public void validatePendingBid(UUID bidderId, BigDecimal bidPrice, LocalDateTime now) {
        if (this.sellerId.equals(bidderId)) {
            throw new SelfBidNotAllowedException();
        }
        if (this.status != AuctionStatus.ONGOING) {
            throw new AuctionNotOngoingException();
        }
        if (bidPrice.remainder(BID_PRICE_UNIT).signum() != 0) {
            throw new BidPriceUnitNotMetException();
        }
        if (!meetsMinimumIncrement(bidPrice)) {
            throw new BidIncrementNotMetException();
        }
        extendTimeIfNearEnd(now);
        this.updatedAt = now;
    }

    public void updateHighestPrice(BigDecimal bidPrice) {
        this.currentHighestPrice = bidPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeToPendingPayment() {
        if (this.status != AuctionStatus.ONGOING) {
            throw new IllegalStateException("진행 중인 경매만 결제 대기로 전환할 수 있습니다");
        }
        this.status = AuctionStatus.PENDING_PAYMENT;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeToFailed() {
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

    private boolean meetsMinimumIncrement(BigDecimal bidPrice) {
        if (this.currentHighestPrice == null) {
            return bidPrice.compareTo(this.startPrice) >= 0;
        }
        return bidPrice.compareTo(this.currentHighestPrice.add(this.bidUnit)) >= 0;
    }

    private void extendTimeIfNearEnd(LocalDateTime now) {
        long remaining = Duration.between(now, this.endedAt).toSeconds();
        if (remaining > 0 && remaining <= EXTEND_THRESHOLD_SECONDS) {
            this.endedAt = this.endedAt.plusSeconds(EXTEND_AMOUNT_SECONDS);
        }
    }
}
