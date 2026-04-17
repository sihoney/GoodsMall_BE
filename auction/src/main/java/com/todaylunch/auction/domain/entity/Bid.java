package com.todaylunch.auction.domain.entity;

import com.todaylunch.auction.domain.enumtype.BidStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "bid")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid {

    @Id
    @Column(name = "bid_id", nullable = false, updatable = false)
    private UUID bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "bidder_id", nullable = false)
    private UUID bidderId;

    @Column(name = "bid_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal bidPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BidStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Bid(
            UUID bidId,
            Auction auction,
            UUID bidderId,
            BigDecimal bidPrice,
            BidStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.bidId = Objects.requireNonNull(bidId);
        this.auction = Objects.requireNonNull(auction);
        this.bidderId = Objects.requireNonNull(bidderId);
        this.bidPrice = Objects.requireNonNull(bidPrice);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /** 초기 상태(ACTIVE + 타임스탬프) 불변성을 팩토리로 강제. 입찰가 양수 검증 포함. */
    public static Bid place(Auction auction, UUID bidderId, BigDecimal bidPrice) {
        if (bidPrice == null || bidPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("입찰가는 0보다 커야 합니다");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Bid(
                UUID.randomUUID(),
                auction,
                bidderId,
                bidPrice,
                BidStatus.ACTIVE,
                now,
                now
        );
    }

    public void outbid() {
        if (this.status == BidStatus.OUTBID) {
            return;
        }
        if (this.status != BidStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 입찰만 OUTBID 처리할 수 있습니다");
        }
        this.status = BidStatus.OUTBID;
        this.updatedAt = LocalDateTime.now();
    }

    public void win() {
        if (this.status != BidStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 입찰만 낙찰 처리할 수 있습니다");
        }
        this.status = BidStatus.WINNING;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != BidStatus.WINNING) {
            throw new IllegalStateException("낙찰 상태의 입찰만 취소할 수 있습니다");
        }
        this.status = BidStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
    }

    public void paid() {
        if (this.status != BidStatus.WINNING) {
            throw new IllegalStateException("낙찰 상태의 입찰만 결제 완료 처리할 수 있습니다");
        }
        this.status = BidStatus.PAYMENT_COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
}
