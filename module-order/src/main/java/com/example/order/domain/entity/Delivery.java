package com.example.order.domain.entity;

import com.example.order.domain.enumtype.DeliveryStatus;
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
@Table(name = "delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "courier")
    private String courier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeliveryStatus status;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Delivery(
            UUID deliveryId,
            UUID sellerId,
            UUID orderItemId,
            String courier,
            String trackingNumber,
            DeliveryStatus status,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.deliveryId = Objects.requireNonNull(deliveryId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.courier = courier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Delivery create(
            UUID deliveryId,
            UUID sellerId,
            UUID orderItemId,
            String courier,
            String trackingNumber,
            DeliveryStatus status,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Delivery(
                deliveryId,
                sellerId,
                orderItemId,
                courier,
                trackingNumber,
                status,
                shippedAt,
                deliveredAt,
                createdAt,
                updatedAt
        );
    }

    public void updateTracking(String courier, String trackingNumber, LocalDateTime updatedAt) {
        this.courier = courier;
        this.trackingNumber = trackingNumber;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(DeliveryStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
