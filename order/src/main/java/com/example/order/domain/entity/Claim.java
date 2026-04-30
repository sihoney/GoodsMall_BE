package com.example.order.domain.entity;

import com.example.order.domain.enumtype.ClaimStatus;
import com.example.order.domain.enumtype.ClaimType;
import com.example.order.domain.enumtype.RequesterType;
import com.example.order.domain.enumtype.ResponsibilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "claims", schema = "order_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Claim {

    @Id
    @Column(name = "claim_id", nullable = false, updatable = false)
    private UUID claimId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ClaimType type;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "detail_reason", length = 500)
    private String detailReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "requester_type", nullable = false, length = 20)
    private RequesterType requesterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsibility_type", length = 20)
    private ResponsibilityType responsibilityType;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Claim(
            UUID claimId,
            OrderItem orderItem,
            UUID sellerId,
            ClaimType type,
            String reason,
            String detailReason,
            ClaimStatus status,
            RequesterType requesterType,
            ResponsibilityType responsibilityType,
            String rejectReason,
            LocalDateTime requestedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.claimId = Objects.requireNonNull(claimId);
        this.orderItem = Objects.requireNonNull(orderItem);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.type = Objects.requireNonNull(type);
        this.reason = Objects.requireNonNull(reason);
        this.detailReason = detailReason;
        this.status = Objects.requireNonNull(status);
        this.requesterType = Objects.requireNonNull(requesterType);
        this.responsibilityType = responsibilityType;
        this.rejectReason = rejectReason;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.completedAt = completedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Claim create(
            OrderItem orderItem,
            UUID sellerId,
            ClaimType type,
            String reason,
            String detailReason,
            RequesterType requesterType,
            ResponsibilityType responsibilityType
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Claim(
                UUID.randomUUID(),
                orderItem,
                sellerId,
                type,
                reason,
                detailReason,
                ClaimStatus.REQUESTED,
                requesterType,
                responsibilityType,
                null,
                now,
                null,
                now,
                now
        );
    }

    public void assignResponsibility(ResponsibilityType type) {
        this.responsibilityType = Objects.requireNonNull(type);
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ClaimStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String rejectReason) {
        this.status = ClaimStatus.REJECTED;
        this.rejectReason = Objects.requireNonNull(rejectReason);
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
