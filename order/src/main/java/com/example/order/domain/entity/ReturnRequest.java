package com.example.order.domain.entity;

import com.example.order.domain.enumtype.InspectionResult;
import com.example.order.domain.enumtype.InspectionStatus;
import com.example.order.domain.enumtype.PickupType;
import com.example.order.domain.enumtype.ReturnRequestStatus;
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
@Table(name = "return_request", schema = "order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnRequest {

    @Id
    @Column(name = "return_request_id", nullable = false, updatable = false)
    private UUID returnRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "carrier", length = 50)
    private String carrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReturnRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "pickup_type", length = 20)
    private PickupType pickupType;

    @Column(name = "pickup_requested_at")
    private LocalDateTime pickupRequestedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "return_completed_at")
    private LocalDateTime returnCompletedAt;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Column(name = "return_address_snapshot", length = 500)
    private String returnAddressSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status", length = 20)
    private InspectionStatus inspectionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_result", length = 10)
    private InspectionResult inspectionResult;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private ReturnRequest(
            UUID returnRequestId,
            Claim claim,
            OrderItem orderItem,
            UUID sellerId,
            String carrier,
            String trackingNumber,
            ReturnRequestStatus status,
            PickupType pickupType,
            LocalDateTime pickupRequestedAt,
            LocalDateTime pickedUpAt,
            LocalDateTime receivedAt,
            LocalDateTime returnCompletedAt,
            String failReason,
            String returnAddressSnapshot,
            InspectionStatus inspectionStatus,
            InspectionResult inspectionResult,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.returnRequestId = Objects.requireNonNull(returnRequestId);
        this.claim = Objects.requireNonNull(claim);
        this.orderItem = Objects.requireNonNull(orderItem);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = Objects.requireNonNull(status);
        this.pickupType = pickupType;
        this.pickupRequestedAt = pickupRequestedAt;
        this.pickedUpAt = pickedUpAt;
        this.receivedAt = receivedAt;
        this.returnCompletedAt = returnCompletedAt;
        this.failReason = failReason;
        this.returnAddressSnapshot = returnAddressSnapshot;
        this.inspectionStatus = inspectionStatus;
        this.inspectionResult = inspectionResult;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ReturnRequest create(
            Claim claim,
            OrderItem orderItem,
            UUID sellerId,
            PickupType pickupType,
            String returnAddressSnapshot
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new ReturnRequest(
                UUID.randomUUID(),
                claim,
                orderItem,
                sellerId,
                null,
                null,
                ReturnRequestStatus.REQUESTED,
                pickupType,
                null,
                null,
                null,
                null,
                null,
                returnAddressSnapshot,
                null,
                null,
                now,
                now
        );
    }

    public void requestPickup() {
        this.status = ReturnRequestStatus.PICKUP_REQUESTED;
        this.pickupRequestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmPickup() {
        this.status = ReturnRequestStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void registerTracking(String carrier, String trackingNumber) {
        this.carrier = Objects.requireNonNull(carrier);
        this.trackingNumber = Objects.requireNonNull(trackingNumber);
        this.updatedAt = LocalDateTime.now();
    }

    public void receive() {
        this.status = ReturnRequestStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
        this.inspectionStatus = InspectionStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeInspection(InspectionResult result) {
        this.inspectionStatus = InspectionStatus.COMPLETED;
        this.inspectionResult = Objects.requireNonNull(result);
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReturnRequestStatus.COMPLETED;
        this.returnCompletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String failReason) {
        this.status = ReturnRequestStatus.FAILED;
        this.failReason = Objects.requireNonNull(failReason);
        this.updatedAt = LocalDateTime.now();
    }
}
